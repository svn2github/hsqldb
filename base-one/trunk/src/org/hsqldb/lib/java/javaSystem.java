/* Copyright (c) 2001-2003, The HSQL Development Group
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


package org.hsqldb.lib.java;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.lang.reflect.Method;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// fredt@users 20021030 - patch 1.7.2 - updates

/**
 * Handles the differences between JDK 1.1.x and 1.2.x and above
 * JDBC 2 methods can now be called from JDK 1.1.x - see javadoc comments
 * for org.hsqldb.jdbcXXXX classes.<p>
 * HSQLDB should no longer be compiled with JDK 1.1.x. In verison 1.7.2 and
 * above, an HSQLDB jar compiled with JDK 1.2 or 1.3 can be used with
 * JRE 1.1.X<p>
 *
 * @author fredt@users
 * @version 1.7.2
 */
public class javaSystem {

    private static Method setLogMethod = null;

    static {
        try {
            setLogMethod =
                java.sql.DriverManager.class.getMethod("setLogWriter",
                    new Class[]{ PrintWriter.class });
        } catch (NoSuchMethodException e) {}
        catch (SecurityException e) {}
    }

    public static void setLogToSystem(boolean value) {

        if (setLogMethod == null) {
            PrintStream newOutStream = (value) ? System.out
                                               : null;

            DriverManager.setLogStream(newOutStream);
        } else {
            try {
                PrintWriter newPrintWriter = (value)
                                             ? new PrintWriter(System.out)
                                             : null;

                setLogMethod.invoke(null, new Object[]{ newPrintWriter });
            } catch (java.lang.reflect.InvocationTargetException e) {}
            catch (IllegalAccessException e) {}
        }
    }
}
