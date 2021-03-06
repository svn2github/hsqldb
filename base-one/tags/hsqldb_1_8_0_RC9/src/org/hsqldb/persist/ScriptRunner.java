/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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


package org.hsqldb.persist;

import org.hsqldb.Database;
import org.hsqldb.HsqlException;
import org.hsqldb.Result;
import org.hsqldb.ResultConstants;
import org.hsqldb.Session;
import org.hsqldb.Trace;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.scriptio.ScriptReaderBase;
import org.hsqldb.scriptio.ScriptWriterBase;

/**
 * Restores the state of a Database instance from an SQL log file. <p>
 *
 * The log file is always plain text.
 *
 * If there is an error, processing stops at that line and the message is
 * logged to the application log. If memory runs out, an exception is thrown.
 *
 * @author fredt@users
 * @version 1.8.0
 */
public class ScriptRunner {

    /**
     *  This is used to read the *.log file and manage any necessary
     *  transaction rollback.
     *
     * @throws  HsqlException
     */
    public static void runScript(Database database, String logFilename,
                                 int logType) throws HsqlException {

        IntKeyHashMap sessionMap = new IntKeyHashMap();
        Session sysSession = database.getSessionManager().getSysSession();
        Session       current    = sysSession;
        int           currentId  = 0;

        database.setReferentialIntegrity(false);

        ScriptReaderBase scr = null;

        try {
            StopWatch sw = new StopWatch();

            scr = ScriptReaderBase.newScriptReader(
                database, logFilename, ScriptWriterBase.SCRIPT_TEXT_170);

            while (scr.readLoggedStatement()) {
                int sessionId = scr.getSessionNumber();

                if (currentId != sessionId) {
                    currentId = sessionId;
                    current   = (Session) sessionMap.get(currentId);

                    if (current == null) {
                        current =
                            database.getSessionManager().newSession(database,
                                sysSession.getUser(), false);

                        sessionMap.put(currentId, current);
                    }
                }

                if (current.isClosed()) {
                    sessionMap.remove(currentId);

                    continue;
                }

                Result result = null;

                switch (scr.getStatementType()) {

                    case ScriptReaderBase.ANY_STATEMENT :
                        result = current.sqlExecuteDirectNoPreChecks(
                            scr.getLoggedStatement());

                        if (result != null
                                && result.mode == ResultConstants.ERROR) {
                            if (result.getException() != null) {
                                throw result.getException();
                            }

                            throw Trace.error(result);
                        }
                        break;

                    case ScriptReaderBase.SEQUENCE_STATEMENT :
                        scr.getCurrentSequence().reset(
                            scr.getSequenceValue());
                        break;

                    case ScriptReaderBase.COMMIT_STATEMENT :
                        current.commit();
                        break;

                    case ScriptReaderBase.INSERT_STATEMENT : {
                        Object[] data = scr.getData();

                        scr.getCurrentTable().insertNoCheckFromLog(current,
                                data);

                        break;
                    }
                    case ScriptReaderBase.DELETE_STATEMENT : {
                        Object[] data = scr.getData();

                        scr.getCurrentTable().deleteNoCheckFromLog(current,
                                data);

                        break;
                    }
                }

                if (current.isClosed()) {
                    sessionMap.remove(currentId);
                }
            }
        } catch (Throwable e) {
            String message;

            // catch out-of-memory errors and terminate
            if (e instanceof OutOfMemoryError) {
                message = "out of memory processing " + logFilename
                          + " line: " + scr.getLineNumber();

                database.logger.appLog.logContext(message);

                throw Trace.error(Trace.OUT_OF_MEMORY);
            }

            // stop processing on bad log line
            message = logFilename + " line: " + scr.getLineNumber() + " "
                      + e.getMessage();

            database.logger.appLog.logContext(message);
            Trace.printSystemOut(message);
        } finally {
            scr.close();
            database.getSessionManager().closeAllSessions();
            database.setReferentialIntegrity(true);
        }
    }
}
