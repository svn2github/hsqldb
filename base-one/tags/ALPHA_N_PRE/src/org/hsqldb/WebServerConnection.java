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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.StringTokenizer;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.resources.BundleHandler;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.OutputStream;

// fredt@users 20021002 - patch 1.7.1 - changed notification method
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// boucherb@users 20030510 - patch 1.7.2 - SSL support => factory interface
// boucherb@users 20030510 - patch 1.7.2 - general lint removal
// boucherb@users 20030514 - patch 1.7.2 - localized error responses

/**
 *  A web server connection is a transient object that lasts for the duration
 *  of the SQL call and its result. This class uses the notification
 *  mechanism in WebServer to allow cleanup after a SHUTDOWN. (fredt@users)
 *
 * @version  1.7.1
 */
class WebServerConnection implements Runnable, ServerConstants {

    static final String               ENCODING = "8859_1";
    private Socket                    mSocket;
    private Server                    mServer;
    private int                       bhnd;
    private static final StringBuffer sb          = new StringBuffer();
    private static final int          GET         = 1;
    private static final int          HEAD        = 2;
    private static final int          POST        = 3;
    private static final int          BAD_REQUEST = 400;
    private static final int          FORBIDDEN   = 403;
    private static final int          NOT_FOUND   = 404;
    private static final String       okh         = "HTTP/1.0 200 OK";
    private static final String       brh = "HTTP/1.0 400 Bad Request";
    private static final String       nfh         = "HTTP/1.0 404 Not Found";
    private static final String       fbh         = "HTTP/1.0 403 Forbidden";
    private static final String ctaoscl =
        "Content-Type: application/octet-stream\nContent-Length: ";

    /**
     *  Constructor declaration
     *
     * @param  socket
     * @param  server
     */
    WebServerConnection(Socket socket, Server server) {

        ClassLoader cl;

        mServer = server;
        mSocket = socket;
        cl      = null;

        try {
            cl = getClass().getClassLoader();
        } catch (Exception e) {}

        bhnd = BundleHandler.getBundleHandle("webserver", cl);
    }

    /**
     * @throws Exception
     * @return
     */
    private BufferedReader newInputStreamReader() throws Exception {
        return new BufferedReader(
            new InputStreamReader(mSocket.getInputStream(), ENCODING));
    }

    private String getMimeType(String name) {

        int    pos;
        String key;
        String mimeType;

        pos      = pos = name == null ? -1
                                      : name.lastIndexOf(".");
        mimeType = null;

        if (pos < 0) {

            // do nothing
        } else {
            key      = name.substring(pos).toLowerCase();
            mimeType = mServer.serverProperties.getProperty(key);
        }

        if (mimeType == null) {

            // CHECKME:  return error response?
            mimeType = SC_DEFAULT_WEB_MIME;
        }

        return mimeType;
    }

    /**
     * Retrieves an HTTP content type / content length line, given
     * the supplied content name and length arguments.
     *
     * @param name the name of the content
     * @param len the length of the content
     * @return
     */
    private String getCTCL(String name, int len) {

        String out;

        synchronized (sb) {
            sb.setLength(0);
            sb.append("Content-Type: ").append(getMimeType(name)).append(
                "\nContent-Length: ").append(len);

            out = sb.toString();
        }

        return out;
    }

    /**
     * Retrieves a new input stream for reading the file with the specified
     * name.
     *
     * @param name the name of the file
     * @throws Exception if a file i/o exception occurs
     * @return a new input stream for reading the named file
     */
    private static InputStream newFileInputStream(String name)
    throws Exception {
        return new BufferedInputStream(new FileInputStream(new File(name)));
    }

    /**
     * Retrieves a new output stream for writing to the connected socket.
     *
     * @throws Exception if an i/o or network exception occurs
     * @return a new output stream
     */
    private DataOutputStream newDataOutputStream() throws Exception {
        return new DataOutputStream(
            new BufferedOutputStream(mSocket.getOutputStream()));
    }

    /**
     * Causes this WebServerConnection to process its HTTP request
     * in a blocking fashion until the request is fully processed
     * or an exception occurs internally.
     */
    public void run() {

        try {
            runImpl();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mServer.notify(SC_CONNECTION_CLOSED);
        }
    }

    /**
     * Internal run implementation.
     *
     * @throws Exception if there is a problem processing this connection's
     *      HTTP request.
     */
    protected void runImpl() throws Exception {

        BufferedReader  input;
        String          request;
        String          name;
        int             method;
        int             len;
        StringTokenizer tokenizer;
        String          first;

        input  = newInputStreamReader();
        name   = null;
        method = BAD_REQUEST;
        len    = -1;

        while (true) {
            request = input.readLine();

            if (request == null) {
                break;
            }

            tokenizer = new StringTokenizer(request, " ");

            if (!tokenizer.hasMoreTokens()) {
                break;
            }

            first = tokenizer.nextToken();

            if (first.equals("GET")) {
                method = GET;
                name   = tokenizer.nextToken();
            } else if (first.equals("HEAD")) {
                method = HEAD;
                name   = tokenizer.nextToken();
            } else if (first.equals("POST")) {
                method = POST;
                name   = tokenizer.nextToken();
            } else if (request.toUpperCase().startsWith("CONTENT-LENGTH:")) {
                len = Integer.parseInt(tokenizer.nextToken());
            }
        }

        switch (method) {

            case BAD_REQUEST :
                processError(BAD_REQUEST);
                break;

            case GET :
                processGet(name, true);
                break;

            case HEAD :
                processGet(name, false);
                break;

            case POST :
                processPost(input, name, len);
                break;
        }

        input.close();
    }

    /**
     *  Processes an HTTP GET request
     *
     * @param  name the name of the content to get
     * @param  send whether to send the content as well, or just the header
     */
    private void processGet(String name, boolean send) {

        try {
            processGetImpl(name, send);
        } catch (Exception e) {
            mServer.traceError("processGet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Internal get implementation.
     *
     * @param  name the name of the content to get
     * @param  send whether to send the content as well, or just the header
     * @throws Exception if there is a problem processing the get
     */
    private void processGetImpl(String name, boolean send) throws Exception {

        String           hdr;
        DataOutputStream os;
        InputStream      is;
        int              b;

        if (name.endsWith("/")) {
            name = name + mServer.getDefaultWebPage();
        }

        // traversing up the directory structure is forbidden.
        if (name.indexOf("..") != -1) {
            processError(FORBIDDEN);

            return;
        }

        name = mServer.getWebRoot() + name;

        if (mServer.mPathSeparatorChar != '/') {
            name = name.replace('/', mServer.mPathSeparatorChar);
        }

        is = null;

        try {
            is  = newFileInputStream(name);
            hdr = getHead(okh, getCTCL(name, is.available()));
        } catch (IOException e) {
            processError(NOT_FOUND);

            return;
        }

        os = newDataOutputStream();

        os.write(hdr.getBytes(ENCODING));

        // TODO:
        // Although we're using buffered input and output streams,
        // this can still be made more efficient by using our own
        // byte buffer and getting closer to the native bulk read
        // and write methods.  Serveral thousand calls to byte read()
        // and writeByte(b), even when backed by buffers, are always 
        // bound to be less efficient than direct read(byte[]) and 
        // write(byte[]) calls.
        if (send) {
            while (-1 != (b = is.read())) {
                os.writeByte(b);
            }
        }

        os.flush();
        os.close();
    }

    /**
     *  Method declaration
     *
     * @param  start
     * @param  end
     * @return
     */
    private String getHead(String start, String end) {

        String out;

        synchronized (sb) {
            sb.setLength(0);
            sb.append(start).append(
                "\nAllow: GET, HEAD, POST\nMIME-Version: 1.0\nServer: ")
                    .append(mServer.mServerName).append('\n').append(
                        end).append("\n\n");

            out = sb.toString();
        }

        return out;
    }

    /**
     *  Method declaration
     *
     * @param  input
     * @param  name
     * @param  len
     */
    private void processPost(BufferedReader input, String name,
                             int len) throws SQLException, IOException {

        char   cbuf[];
        String s;
        int    p;
        int    q;
        String user;
        String password;

        if (len < 0) {
            processError(BAD_REQUEST);

            return;
        }

        cbuf = new char[len];

        try {
            input.read(cbuf, 0, len);
        } catch (IOException e) {
            processError(BAD_REQUEST);

            return;
        }

        s = new String(cbuf);
        p = s.indexOf('+');
        q = s.indexOf('+', p + 1);

        if ((p == -1) || (q == -1)) {
            processError(BAD_REQUEST);

            return;
        }

        // TODO:
        // Base64 encoding and decoding would make more efficient use
        // of the character set and would reduce network traffic.
        // Encoding to hexidicimal effectively doubles the size
        // of a unicode string, while Base64 causes inflation by only
        // a factor of 1.333... (every six bits becomes one byte).
        // Use of compressed streams might be nice also.
        // However, both paths imposes yet another step, more library code
        // and, for compressed streams, an extra memory buffer allocation,
        // reducing server-side processing speed and memory efficiency
        // while possibly improving network load.
        user     = StringConverter.hexStringToUnicode(s.substring(0, p));
        password = StringConverter.hexStringToUnicode(s.substring(p + 1, q));
        s        = StringConverter.hexStringToUnicode(s.substring(q + 1));

        processQuery(user, password, s);
    }

    /**
     *  Method declaration
     *
     * @param  code
     */
    private void processError(int code) {

        String           msg;
        DataOutputStream os;

        mServer.trace("processError " + code);

        switch (code) {

            case BAD_REQUEST :
                msg = getHead(brh, "");
                msg += BundleHandler.getString(bhnd, "BAD_REQUEST");
                break;

            case NOT_FOUND :
                msg = getHead(nfh, "");
                msg += BundleHandler.getString(bhnd, "NOT_FOUND");
                break;

            case FORBIDDEN :
                msg = getHead(fbh, "");
                msg += BundleHandler.getString(bhnd, "FORBIDDEN");
                break;

            default :
                msg = null;
        }

        try {
            os = newDataOutputStream();

            os.write(msg.getBytes(ENCODING));
            os.flush();
            os.close();
        } catch (Exception e) {
            mServer.traceError("processError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     *  Method declaration
     *
     * @param  user
     * @param  password
     * @param  statement
     */
    private void processQuery(String user, String password,
                              String statement) {

        byte             result[];
        int              len;
        String           header;
        DataOutputStream output;

        try {
            mServer.trace(statement);

            // TODO:  Stream the underlying result object instead of allocating
            //        a potentially large intermediate byte-buffer
            result = mServer.mDatabase.execute(user, password, statement);
            len    = result.length;
            header = getHead(okh, ctaoscl + len);
            output = newDataOutputStream();

            output.write(header.getBytes(ENCODING));
            output.write(result);
            output.flush();
            output.close();
        } catch (Exception e) {
            mServer.traceError("processQuery: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
