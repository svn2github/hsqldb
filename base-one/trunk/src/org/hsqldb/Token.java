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

import org.hsqldb.lib.HsqlObjectToIntMap;

/**
 * Provides an enumeration of the token types commonly encountered
 * while processing database commands. <p>
 *
 * @author  Nitin Chauhan
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
class Token {

    static final int          UNKNOWN               = -1;
    static final int          CALL                  = 1;
    static final int          CHECKPOINT            = 2;
    static final int          COMMIT                = 3;
    static final int          CONNECT               = 4;
    static final int          CREATE                = 5;
    static final int          DELETE                = 6;
    static final int          DISCONNECT            = 7;
    static final int          DROP                  = 8;
    static final int          GRANT                 = 9;
    static final int          INSERT                = 10;
    static final int          REVOKE                = 11;
    static final int          ROLLBACK              = 12;
    static final int          SAVEPOINT             = 13;
    static final int          SCRIPT                = 14;
    static final int          SELECT                = 15;
    static final int          SET                   = 16;
    static final int          SHUTDOWN              = 17;
    static final int          UPDATE                = 18;
    static final int          SEMICOLON             = 19;
    static final int          ALTER                 = 20;
    static final int          ADD                   = 24;
    static final int          ALIAS                 = 35;
    static final int          AUTOCOMMIT            = 43;
    static final int          CACHED                = 31;
    static final int          COLUMN                = 27;
    static final int          CONSTRAINT            = 25;
    static final int          FOREIGN               = 26;
    static final int          IGNORECASE            = 41;
    static final int          INDEX                 = 22;
    static final int          LOGSIZE               = 39;
    static final int          LOGTYPE               = 40;
    static final int          MAXROWS               = 42;
    static final int          MEMORY                = 30;
    static final int          PASSWORD              = 37;
    static final int          PRIMARY               = 36;
    static final int          PROPERTY              = 47;
    static final int          READONLY              = 38;
    static final int          REFERENTIAL_INTEGRITY = 46;
    static final int          RENAME                = 23;
    static final int          SOURCE                = 44;
    static final int          TABLE                 = 21;
    static final int          TEXT                  = 29;
    static final int          TRIGGER               = 33;
    static final int          UNIQUE                = 28;
    static final int          USER                  = 34;
    static final int          VIEW                  = 32;
    static final int          WRITE_DELAY           = 45;
    private static HsqlObjectToIntMap commandSet;

    static {
        commandSet = newCommandSet();
    }

    /**
     * Retrieves a new map from set of string tokens to numeric tokens for
     * commonly encountered database command token occurences.
     *
     * @return a new map for the database command token set
     */
    private static HsqlObjectToIntMap newCommandSet() {

        HsqlObjectToIntMap commandSet;

        commandSet = new HsqlObjectToIntMap(67);

        commandSet.put("ALTER", ALTER);
        commandSet.put("CALL", CALL);
        commandSet.put("CHECKPOINT", CHECKPOINT);
        commandSet.put("COMMIT", COMMIT);
        commandSet.put("CONNECT", CONNECT);
        commandSet.put("CREATE", CREATE);
        commandSet.put("DELETE", DELETE);
        commandSet.put("DISCONNECT", DISCONNECT);
        commandSet.put("DROP", DROP);
        commandSet.put("GRANT", GRANT);
        commandSet.put("INSERT", INSERT);
        commandSet.put("REVOKE", REVOKE);
        commandSet.put("ROLLBACK", ROLLBACK);
        commandSet.put("SAVEPOINT", SAVEPOINT);
        commandSet.put("SCRIPT", SCRIPT);
        commandSet.put("SELECT", SELECT);
        commandSet.put("SET", SET);
        commandSet.put("SHUTDOWN", SHUTDOWN);
        commandSet.put("UPDATE", UPDATE);
        commandSet.put(";", SEMICOLON);

        //
        commandSet.put("TABLE", TABLE);
        commandSet.put("INDEX", INDEX);
        commandSet.put("RENAME", RENAME);
        commandSet.put("ADD", ADD);
        commandSet.put("CONSTRAINT", CONSTRAINT);
        commandSet.put("FOREIGN", FOREIGN);
        commandSet.put("COLUMN", COLUMN);
        commandSet.put("UNIQUE", UNIQUE);
        commandSet.put("TEXT", TEXT);
        commandSet.put("MEMORY", MEMORY);
        commandSet.put("CACHED", CACHED);
        commandSet.put("VIEW", VIEW);
        commandSet.put("TRIGGER", TRIGGER);
        commandSet.put("USER", USER);
        commandSet.put("ALIAS", ALIAS);
        commandSet.put("PASSWORD", PASSWORD);
        commandSet.put("PRIMARY", PRIMARY);
        commandSet.put("PROPERTY", PROPERTY);
        commandSet.put("READONLY", READONLY);
        commandSet.put("LOGSIZE", LOGSIZE);
        commandSet.put("LOGTYPE", LOGTYPE);
        commandSet.put("IGNORECASE", IGNORECASE);
        commandSet.put("MAXROWS", MAXROWS);
        commandSet.put("AUTOCOMMIT", AUTOCOMMIT);
        commandSet.put("SOURCE", SOURCE);
        commandSet.put("WRITE_DELAY", WRITE_DELAY);
        commandSet.put("REFERENTIAL_INTEGRITY", REFERENTIAL_INTEGRITY);

        return commandSet;
    }

    static int get(String token) {
      return commandSet.get(token);
    }
}
