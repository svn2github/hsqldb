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

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.IntKeyHashMap;
import java.io.IOException;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.store.ValuePool;

class ScriptRunner {

    /**
     *  This is used to read the *.log file and manage any necessary
     *  transaction rollback.
     *
     * @throws  HsqlException
     */
    static void runScript(Database database, String scriptFilename,
                          int logType) throws HsqlException {

        if (database.filesInJar) {
            if (ScriptRunner.class.getClassLoader().getResource(
                    scriptFilename) == null) {
                return;
            }
        } else if (!FileUtil.exists(scriptFilename)) {
            return;
        }

        IntKeyHashMap sessionMap = new IntKeyHashMap();
        Session       sysSession = database.sessionManager.getSysSession();
        Session       current    = sysSession;

        database.setReferentialIntegrity(false);

        try {
            StopWatch sw = new StopWatch();
            DatabaseScriptReader scr =
                DatabaseScriptReader.newDatabaseScriptReader(database,
                    scriptFilename, logType);

            while (true) {
                String s = scr.readLoggedStatement();

                if (s == null) {
                    break;
                }

                if (s.startsWith("/*C")) {
                    int id = Integer.parseInt(s.substring(3, s.indexOf('*',
                        4)));

                    current = (Session) sessionMap.get(id);

                    if (current == null) {
                        current = database.sessionManager.newSession(database,
                                sysSession.getUser(), false);

                        sessionMap.put(id, current);
                    }

                    s = s.substring(s.indexOf('/', 1) + 1);
                }

                if (s.length() != 0) {
                    Result result = current.sqlExecuteDirectNoPreChecks(s);

                    if (result != null
                            && result.iMode == ResultConstants.ERROR) {

/** @todo fredt - must catch out of  memory errors and terminate */
/* boucherb - Result(OOME,sql) now sets vendor code to Trace.OUT_OF_MEMORY */                               
                        Trace.printSystemOut("error in " + scriptFilename
                                             + " line: "
                                             + scr.getLineNumber());
                        Trace.printSystemOut(result.mainString);
                    }
                }
            }

            scr.close();
            database.sessionManager.closeAllSessions();

            if (Trace.TRACE) {
                Trace.trace("restore time: " + sw.elapsedTime());
            }
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, scriptFilename + " " + e);
        }

        database.setReferentialIntegrity(true);
    }
}
