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

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.hsqldb.lib.StringConverter;

// fredt@users 20020130 - patch 475586 by wreissen@users
// fredt@users 20020328 - patch 1.7.0 by fredt - error trapping
// fredt@users 20030630 - patch 1.7.2 - new protocol, persistent sessions

/**
 * Servlet can act as an interface between the client and the database for the
 * the client / server mode of HSQL Database Engine. It uses the HTTP protocol
 * for communication. This class is not required if the included HSQLDB
 * Weberver is used on the server host. But if the host is running a J2EE
 * application server or a servlet container such as Tomcat, the Servlet class
 * can be hosted on this server / container to serve external requests from
 * external hosts.<p>
 * The remote applet / application should
 * use the normal JDBC interfaces to connect to the URL of this servlet. An
 * example URL is:
 * <pre>
 * jdbc:hsqldb:http://localhost.com:8080/servlet/org.hsqldb.Servlet
 * </pre>
 * The database name is taken from the servlet engine property:
 * <pre>
 * hsqldb.server.database
 * </pre>
 * <p>
 * From version 1.7.2 JDBC connections via the HTTP protocol are persistent
 * in the JDBC sense. The JDBC Connection that is established can support
 * transactions spanning several Statement calls and real PreparedStatement
 * calls are supported. This class has been rewritten to support the new
 * features. (fredt@users)
 *
 * @version 1.7.2
 */
public class Servlet extends javax.servlet.http.HttpServlet {

    static final int      BUFFER_SIZE = 256;
    String                dbType;
    String                dbPath;
    String                errorStr;
    BinaryServerRowOutput rowOut = new BinaryServerRowOutput(BUFFER_SIZE);
    BinaryServerRowInput  rowIn  = new BinaryServerRowInput(rowOut);

    /**
     * Method declaration
     *
     *
     * @param database
     */
    public void init(ServletConfig config) {

        try {
            super.init(config);
        } catch (ServletException exp) {
            log(exp.getMessage());
        }

        String dbStr = getInitParameter("hsqldb.server.database");

        if (dbStr == null) {
            dbStr = ".";
        }

        HsqlProperties dbURL = DatabaseManager.parseURL(dbStr, false);

        log("Database filename = " + dbStr);

        if (dbURL == null) {
            errorStr = "Bad Database name";

            log(errorStr);
        } else {
            dbPath = dbURL.getProperty("database");
            dbType = dbURL.getProperty("connection_type");
        }

        log("Initialization completed.");
    }

    private static long lModified = 0;

    /**
     * Method declaration
     *
     *
     * @param req
     *
     * @return
     */
    protected long getLastModified(HttpServletRequest req) {

        // this is made so that the cache of the http server is not used
        // maybe there is some other way
        return lModified++;
    }

    /**
     * Method declaration
     *
     *
     * @param request
     * @param response
     *
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
                      throws IOException, ServletException {

        String query = request.getQueryString();

        if ((query == null) || (query.length() == 0)) {
            response.setContentType("text/html");

// fredt@users 20020130 - patch 1.7.0 by fredt
// to avoid caching on the browser
            response.setHeader("Pragma", "no-cache");

            PrintWriter out = response.getWriter();

            out.println(
                "<html><head><title>HSQL Database Engine Servlet</title>");
            out.println("</head><body><h1>HSQL Database Engine Servlet</h1>");
            out.println("The servlet is running.<p>");

            if (errorStr == null) {
                out.println("The database is also running.<p>");
                out.println("Database name: " + dbType + dbPath + "<p>");
                out.println("Queries processed: " + iQueries + "<p>");
            } else {
                out.println("<h2>The database is not running!</h2>");
                out.println("The error message is:<p>");
                out.println(errorStr);
            }

            out.println("</body></html>");
        }
    }

    /**
     * Method declaration
     *
     *
     * @param request
     * @param response
     *
     * @throws IOException
     * @throws ServletException
     */
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
                       throws IOException, ServletException {


        try {
            DataInputStream inStream =
                new DataInputStream(request.getInputStream());

            // fredt@users - the servlet container, Resin does not return all
            // the bytes with one call to input.read(b,0,len) when len > 8192
            // bytes, the loop in the next method handles this
            Result resultIn = HSQLClientConnection.read(rowIn, inStream);
            Result resultOut;

            if (resultIn.iMode == ResultConstants.SQLCONNECT) {
                try {
                    Session session = DatabaseManager.newSession(dbType,
                        dbPath, resultIn.getMainString(),
                        resultIn.getSubString());

                    resultOut = new Result(ResultConstants.UPDATECOUNT);
                    resultOut.sessionID = session.getId();
                } catch (HsqlException e) {
                    resultOut = new Result(e.getMessage(), e.getSQLState(),
                                           e.getErrorCode());
                }
            } else {
                Session session = DatabaseManager.getSession(dbType, dbPath,
                    resultIn.sessionID);

                resultOut = session.execute(resultIn);
            }

            rowOut.reset();
            resultOut.write(rowOut);

            //
            response.setContentType("application/octet-stream");
            response.setContentLength(rowOut.size());

            //
            ServletOutputStream outStream = response.getOutputStream();

            outStream.write(rowOut.getOutputStream().getBuffer(), 0,
                            rowOut.getOutputStream().size());
            outStream.flush();
            outStream.close();

            iQueries++;
        } catch (HsqlException e) {}

        // System.out.print("Queries processed: "+iQueries+"  \n");
    }

    static private int iQueries;
}
