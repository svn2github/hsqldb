/* Copyright (c) 2001-2004, The HSQL Development Group
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

package org.hsqldb.test;

import java.io.File;

public class TestSqlTool extends junit.framework.TestCase {
    SqlToolHarness harness = new SqlToolHarness();

    public void testHistory() throws Exception {
        assertTrue("Recall command from SQL History",
                harness.execute(new File("hist-recall-19.sql")));
        assertTrue("Recall and execute a query from SQL History",
                harness.execute(new File("hist-recall-runquery.sql")));
    }

    public void testEditing() throws Exception {
        assertTrue("s: command, no switches",
                harness.execute(new File("edit-s-noswitches.sql")));
        assertTrue("s: command w/ switches",
                harness.execute(new File("edit-s-switches.sql")));
        assertTrue("a: command",
                harness.execute(new File("edit-a.sql")));
    }

    public void testArgs() throws Exception {
        assertTrue("--noinput command-line switch",
                harness.execute(new File("args-noinput.sql")));
        assertTrue("--sql command-line switch",
                harness.execute(new File("args-sql.sql")));
        assertTrue("--sql AND --noinput command-line switches",
                harness.execute(new File("args-sqlni.sql")));
    }

    public void testComments() throws Exception {
        assertTrue("Comments followed immediately by another command",
                harness.execute(new File("comment-midline.sql")));
    }

    public void testPL() throws Exception {
        assertTrue("PL variable use",
                harness.execute(new File("pl-variable.sql")));
    }

    public void testSpecials() throws Exception {
        assertTrue("\\q command w/ no arg",
                harness.execute(new File("special-q.sql")));
        assertTrue("\\q command w/ arg",
                harness.execute(new File("special-q-arg.sql")));
    }

    public void testSQL() throws Exception {
        assertTrue("Blank line with SQL command, interactive",
                harness.execute(new File("sql-blankint.sql")));
        assertTrue("Blank line with SQL command, file mode",
                harness.execute(new File("sql-blankfile.sql")));
    }

    // public TestSqlTool() { super(); } necessary?
    public TestSqlTool(String s) { super(s); }

    static public void main(String[] sa) {
        if (sa.length > 0 && sa[0].startsWith("--gui")) {
            junit.swingui.TestRunner.run(TestSqlTool.class);
        } else {
            junit.textui.TestRunner runner = new junit.textui.TestRunner();
            System.exit(
                runner.run(
                    runner.getTest(TestSqlTool.class.getName())
                ).wasSuccessful() ? 0 : 1
            );
        }
    }

    static public junit.framework.Test suite() {
        junit.framework.TestSuite newSuite = new junit.framework.TestSuite();
        newSuite.addTest(new TestSqlTool("testHistory"));
        newSuite.addTest(new TestSqlTool("testEditing"));
        newSuite.addTest(new TestSqlTool("testArgs"));
        newSuite.addTest(new TestSqlTool("testComments"));
        newSuite.addTest(new TestSqlTool("testPL"));
        newSuite.addTest(new TestSqlTool("testSpecials"));
        newSuite.addTest(new TestSqlTool("testSQL"));
        return newSuite;
    };
}
