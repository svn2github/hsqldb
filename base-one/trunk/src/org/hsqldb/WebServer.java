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

import java.io.File;
import java.net.Socket;
import org.hsqldb.resources.BundleHandler;

// fredt@users 20020215 - patch 1.7.0 by fredt
// method rorganised to use new HsqlServerProperties class
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// boucherb@users 20030510 - patch 1.7.2 - SSL support moved to factory interface
// boucherb@users 20030510 - patch 1.7.2 - moved all common code to Server
// boucherb@users 20030510 - patch 1.7.2 - general lint removal

/**
 *  WebServer has two distinct functions:<p>
 *
 *  The primary function is to allow client/server access to HSQLDB databases
 *  via the HTTP protocol. This protocol is less efficient than the HSQL
 *  protocol used by the Server class and should be used only in situations
 *  where sandboxes or firewalls between the client and the server do not
 *  allow the use of the HSQL protocol. One example is client/server access by
 *  an applet running in browsers on remote hosts and accessing the database
 *  engine on the HTTP server from which the applet originated. From version
 *  1.7.2 HTTP database connections are persistent and support transactions.
 *  Similar to HSQL connections, they should be explicitly closed to free the
 *  server resources.<p>
 *
 *  The secondary function of WebServer is to act as a simple general purpose
 *  HTTP server. It is aimed to support the minimum requirements set out by
 *  the HTTP/1.0 standard. The HEAD and GET method can be used to query and
 *  retreive static files from the HTTP server.<p>
 *
 *  Both the database server and HTTP server functions of WebServer are
 *  configured with the webserver.properties file. It contains entries for the
 *  database server similar to those for the HSQL protocol Server class. In
 *  addition, a list mapping different file endings to their mime types is
 *  included in this file. (fredt@users) <p>
 *
 *
 *  Example of the webserver.properties file:
 *
 * <pre>
 * server.port=80
 * server.database=test
 * server.root=./
 * server.default_page=index.html
 * server.silent=true
 *
 * .htm=text/html
 * .html=text/html
 * .txt=text/plain
 * .gif=image/gif
 * .class=application/octet-stream
 * .jpg=image/jpeg
 * .jgep=image/jpeg
 * .zip=application/x-zip-compressed
 * </pre>
 *
 *  Forr server.root use / as separator even for DOS/Windows.<br>
 *  file extensions for mime types  must be lowercase<br>
 *
 * @version 1.7.2
 */
public class WebServer extends Server {

    int           bundleHandle;
    protected int serverProtocol = ServerConstants.SC_PROTOCOL_HTTP;

    public WebServer() {

        super(ServerConstants.SC_PROTOCOL_HTTP);

        ClassLoader cl = null;

        try {
            cl = getClass().getClassLoader();
        } catch (Exception e) {}

        bundleHandle = BundleHandler.getBundleHandle("webserver", cl);
    }

    protected Runnable newConnectionHandler(Socket socket) {
        return new WebServerConnection(socket, this);
    }

    /**
     *  Starts a new WebServer.
     *
     * @param  args the "command line" parameters with which to start
     *      the WebServer.  "-?" will cause the command line arguments
     *      help to be printed to the standard output
     */
    public static void main(String args[]) {

        WebServer      server;
        HsqlProperties props;
        String         propsPath;

        if (args.length > 0) {
            String p = args[0];

            if ((p != null) && p.startsWith("-?")) {
                printHelp("webserver.help");

                return;
            }
        }

        props = HsqlProperties.argArrayToProps(args,
                                               ServerConstants.SC_KEY_PREFIX);

        // Standard behaviour when started from the command line
        // is to halt the VM when the server exits.  This may, of
        // course, be partially overridden with a security policy
        props.setPropertyIfNotExists(ServerConstants.SC_KEY_NO_SYSTEM_EXIT,
                                     "false");

        server    = new WebServer();
        propsPath = server.getDefaultPropertiesPath();

        server.print("Invoked from main() method");
        server.print("Loading properties from [" + propsPath + "]");

        if (!server.putPropertiesFromFile(propsPath)) {
            server.print("Could not load properties from file");
            server.print("Using cli/default properties only");
        }

        server.setProperties(props);
        server.start();
    }
}
