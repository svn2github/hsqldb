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

public class TestSqlTool extends junit.framework.TestCase {
    protected void setUp() throws Exception {
        System.err.println("Running setUp()");
    }

    protected void tearDown() {
        System.err.println("Running tearDown()");
    }

    public void testBadOther() throws Exception {
        System.err.println("Running testBadOther()");
        System.err.println("Pre-failure");
        assertTrue("testBadOther failed", false);
        System.err.println("Post-failure");
    }

    public void testGoodBrother() throws Exception {
        System.err.println("Running testGoodBrother()");
        assertTrue("testGoodOther failed", true);
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
        newSuite.addTest(new TestSqlTool("testGoodBrother"));
        newSuite.addTest(new TestSqlTool("testBadOther"));
        return newSuite;
    };
}
