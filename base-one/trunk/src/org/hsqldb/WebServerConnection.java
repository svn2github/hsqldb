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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.HttpURLConnection;
import org.hsqldb.resources.BundleHandler;
import org.hsqldb.lib.InOutUtil;
import org.hsqldb.lib.ArrayUtil;

// fredt@users 20021002 - patch 1.7.1 - changed notification method
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// boucherb@users 20030510 - patch 1.7.2 - SSL support moved to factory interface
// boucherb@users 20030510 - patch 1.7.2 - general lint removal
// boucherb@users 20030514 - patch 1.7.2 - localized error responses
// fredt@users 20030628 - patch 1.7.2 - new protocol, persistent sessions

/**
 *  A web server connection is a transient object that lasts for the duration
 *  of the SQL call and its result. This class uses the notification
 *  mechanism in WebServer to allow cleanup after a SHUTDOWN.<p>
 *
 *  The POST method is used for login  and subsequent remote calls. In 1.7.2
 *  The initial login establishes a persistent Session and returns its handle
 *  to the client. Subsequent calls are executed in the context of this
 *  session.<p>
 *
 *  (fredt@users)
 *
 * @version  1.7.2
 */
class WebServerConnection implements Runnable {

    static final String         ENCODING = "8859_1";
    private Socket              socket;
    private WebServer           server;
    private static final int    REQUEST_TYPE_BAD  = 0;
    private static final int    REQUEST_TYPE_GET  = 1;
    private static final int    REQUEST_TYPE_HEAD = 2;
    private static final int    REQUEST_TYPE_POST = 3;
    private static final String HEADER_OK         = "HTTP/1.0 200 OK";
    private static final String HEADER_BAD_REQUEST =
        "HTTP/1.0 400 Bad Request";
    private static final String HEADER_NOT_FOUND = "HTTP/1.0 404 Not Found";
    private static final String HEADER_FORBIDDEN = "HTTP/1.0 403 Forbidden";
    static final int            BUFFER_SIZE      = 256;
    BinaryServerRowOutput rowOut = new BinaryServerRowOutput(BUFFER_SIZE);
    BinaryServerRowInput        rowIn = new BinaryServerRowInput(rowOut);

    //
    static final byte[] BYTES_GET        = "GET".getBytes();
    static final byte[] BYTES_HEAD       = "HEAD".getBytes();
    static final byte[] BYTES_POST       = "POST".getBytes();
    static final byte[] BYTES_CONTENT    = "Content-Length: ".getBytes();
    static final byte[] BYTES_WHITESPACE = new byte[] {
        ' ', '\t'
    };

    // default mime type mappings
    private static final int hnd_content_types =
        BundleHandler.getBundleHandle("content_types", null);

    /**
     * Creates a new WebServerConnection to the specified WebServer on the
     * specified socket.
     *
     * @param socket the network socket on which WebServer communication
     *      takes place
     * @param server the WebServer instance to which the object
     *      represents a connection 
     */
    WebServerConnection(Socket socket, WebServer server) {
        this.server = server;
        this.socket = socket;
    }

    /**
     * Retrieves a best-guess mime-type string using the file extention
     * of the name argument.
     *
     * @return a best-guess mime-type string using the file extention
     *      of the name argument.
     */
    private String getMimeTypeString(String name) {

        int    pos;
        String key;
        String mimeType;

        if (name == null) {
            return ServerConstants.SC_DEFAULT_WEB_MIME;
        }

        pos      = name.lastIndexOf('.');
        key      = null;
        mimeType = null;

        // first search user-specified mapping
        if (pos >= 0) {
            key      = name.substring(pos).toLowerCase();
            mimeType = server.serverProperties.getProperty(key);
        }

        // if not found, search default mapping
        if (mimeType == null && key.length() > 1) {
            mimeType = BundleHandler.getString(hnd_content_types,
                                               key.substring(1));
        }

        return mimeType == null ? ServerConstants.SC_DEFAULT_WEB_MIME
                                : mimeType;
    }

    /**
     * Causes this WebServerConnection to process its HTTP request
     * in a blocking fashion until the request is fully processed
     * or an exception occurs internally.
     *
     * This method reads the Request line then delegates action to subroutines.
     */
    public void run() {

        try {
            DataInputStream inStream =
                new DataInputStream(socket.getInputStream());
            int    count;
            String request;
            String name   = null;
            int    method = REQUEST_TYPE_BAD;
            int    len    = -1;

            // read any blank lines
            do {
                count = InOutUtil.readLine(inStream, rowOut);
            } while (inStream.available() > 0 && count < 2);

            byte[] byteArray = rowOut.getBuffer();
            int    offset    = rowOut.size() - count;

            if (ArrayUtil.containsAt(byteArray, offset, BYTES_POST)) {
                method = REQUEST_TYPE_POST;
                offset += BYTES_POST.length;
            } else if (ArrayUtil.containsAt(byteArray, offset, BYTES_GET)) {
                method = REQUEST_TYPE_GET;
                offset += BYTES_GET.length;
            } else if (ArrayUtil.containsAt(byteArray, offset, BYTES_HEAD)) {
                method = REQUEST_TYPE_HEAD;
                offset += BYTES_HEAD.length;
            } else {
                throw new Exception();
            }

            count = ArrayUtil.countStartElementsAt(byteArray, offset,
                                                   BYTES_WHITESPACE);

            if (count == 0) {
                throw new Exception();
            }

            offset += count;
            count = ArrayUtil.countNonStartElementsAt(byteArray, offset,
                    BYTES_WHITESPACE);
            name = new String(byteArray, offset, count, ENCODING);

            switch (method) {

                case REQUEST_TYPE_BAD :
                    processError(REQUEST_TYPE_BAD);
                    break;

                case REQUEST_TYPE_GET :
                    processGet(name, true);
                    break;

                case REQUEST_TYPE_HEAD :
                    processGet(name, false);
                    break;

                case REQUEST_TYPE_POST :
                    processPost(inStream, name);
                    break;
            }

            inStream.close();
            socket.close();
        } catch (Exception e) {
            server.printStackTrace(e);
        }
    }

    /**
     * POST is used only for database access. So we can assume the strings
     * are those generated by HTTPClientConnection
     */
    private void processPost(InputStream inStream,
                             String name) throws HsqlException, IOException {

        // fredt - parsing in this block is not actually necessary
        try {

            // read the Content-Type line
            InOutUtil.readLine(inStream, rowOut);

            // read and parse the Content-Length line
            int count  = InOutUtil.readLine(inStream, rowOut);
            int offset = rowOut.size() - count;

            // get buffer always after reading into rowOut, else old buffer may
            // be returned
            byte[] byteArray = rowOut.getBuffer();

            if (!ArrayUtil.containsAt(byteArray, offset, BYTES_CONTENT)) {
                throw new Exception();
            }

            count  -= BYTES_CONTENT.length;
            offset += BYTES_CONTENT.length;

            // omit the last two characters
            String lenStr = new String(byteArray, offset, count - 2);
            int    length = Integer.parseInt(lenStr);

            InOutUtil.readLine(inStream, rowOut);
        } catch (Exception e) {
            processError(HttpURLConnection.HTTP_BAD_REQUEST);

            return;
        }

        processQuery(inStream);
    }

    /**
     * Processes a database query in HSQL protocol that has been
     * tunneled over HTTP protocol.
     *
     * @param inStream the incoming byte stream representing the HSQL protocol
     *      database query
     */
    void processQuery(InputStream inStream) {

        try {
            Result resultIn = Result.read(rowIn,
                                          new DataInputStream(inStream));

            //
            Result resultOut;

            if (resultIn.iMode == ResultConstants.SQLCONNECT) {
                try {
                    int dbIndex = ArrayUtil.find(server.dbAlias,
                                                 resultIn.subSubString);
                    int dbID = server.dbID[dbIndex];
                    Session session = DatabaseManager.newSession(dbID,
                        resultIn.getMainString(), resultIn.getSubString());

                    resultOut = new Result(ResultConstants.UPDATECOUNT);
                    resultOut.databaseID = dbID;
                    resultOut.sessionID  = session.getId();
                } catch (HsqlException e) {
                    resultOut = new Result(e, null);
                } catch (ArrayIndexOutOfBoundsException e) {
                    resultOut = new Result(
                        Trace.getError(Trace.DATABASE_NOT_EXISTS, null),
                        resultIn.subSubString);
                }
            } else {
                int dbIndex = resultIn.databaseID;
                Session session =
                    DatabaseManager.getSession(server.dbType[dbIndex],
                                               server.dbPath[dbIndex],
                                               resultIn.sessionID);

                //server.traceConnection(resultIn.sessionID + " : "
                //                       + resultIn.getMainString());
                resultOut = session.execute(resultIn);
            }

//
            rowOut.reset();
            resultOut.write(rowOut);

            OutputStream outStream = socket.getOutputStream();
            String header = getHead(HEADER_OK, false,
                                    "application/octet-stream",
                                    rowOut.size());

            outStream.write(header.getBytes(ENCODING));
            outStream.write(rowOut.getOutputStream().getBuffer(), 0,
                            rowOut.getOutputStream().size());
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            server.printStackTrace(e);
        }
    }

    /**
     *  Processes an HTTP GET request
     *
     * @param  name the name of the content to get
     * @param  send whether to send the content as well, or just the header
     */
    private void processGet(String name, boolean send) {

        try {
            String       hdr;
            OutputStream os;
            InputStream  is;
            int          b;

            if (name.endsWith("/")) {
                name = name + server.getDefaultWebPage();
            }

            // traversing up the directory structure is forbidden.
            if (name.indexOf("..") != -1) {
                processError(HttpURLConnection.HTTP_FORBIDDEN);

                return;
            }

            name = server.getWebRoot() + name;

            if (File.separatorChar != '/') {
                name = name.replace('/', File.separatorChar);
            }

            is = null;

            try {
                is = new BufferedInputStream(
                    new FileInputStream(new File(name)));
                hdr = getHead(HEADER_OK, true, getMimeTypeString(name),
                              is.available());
            } catch (IOException e) {
                processError(HttpURLConnection.HTTP_NOT_FOUND);

                return;
            }

            os = new BufferedOutputStream(socket.getOutputStream());

            os.write(hdr.getBytes(ENCODING));

            if (send) {
                while ((b = is.read()) != -1) {
                    os.write(b);
                }
            }

            os.flush();
            os.close();
        } catch (Exception e) {
            server.printError("processGet: " + e.getMessage());
            server.printStackTrace(e);
        }
    }

    /**
     * Retrieves an HTTP protocol header given the supplied arguments.
     *
     * @param responseCodeString the HTTP response code
     * @param addInfo true if additional header info is to be added
     * @param mimeType the Content-Type field value
     * @param length the Content-Length field value
     * @return an HTTP protocol header
     */
    String getHead(String responseCodeString, boolean addInfo,
                   String mimeType, int length) {

        StringBuffer sb = new StringBuffer(128);

        sb.append(responseCodeString).append("\r\n");

        if (addInfo) {
            sb.append("Allow: GET, HEAD, POST\nMIME-Version: 1.0\r\n");
            sb.append("Server: ").append(server.serverName).append("\r\n");
        }

        if (mimeType != null) {
            sb.append("Content-Type: ").append(mimeType).append("\r\n");
            sb.append("Content-Length: ").append(length).append("\r\n");
        }

        sb.append("\r\n");

        return sb.toString();
    }

    /**
     *  Processess an HTTP error condition, sending an error response to
     *  the client.
     *
     * @param code the error condition code
     */
    private void processError(int code) {

        String msg;

        server.trace("processError " + code);

        switch (code) {

            case HttpURLConnection.HTTP_BAD_REQUEST :
                msg = getHead(HEADER_BAD_REQUEST, false, null, 0);
                msg += BundleHandler.getString(server.bundleHandle,
                                               "BAD_REQUEST");
                break;

            case HttpURLConnection.HTTP_NOT_FOUND :
                msg = getHead(HEADER_NOT_FOUND, false, null, 0);
                msg += BundleHandler.getString(server.bundleHandle,
                                               "NOT_FOUND");
                break;

            case HttpURLConnection.HTTP_FORBIDDEN :
                msg = getHead(HEADER_FORBIDDEN, false, null, 0);
                msg += BundleHandler.getString(server.bundleHandle,
                                               "FORBIDDEN");
                break;

            default :
                msg = null;
        }

        try {
            OutputStream os =
                new BufferedOutputStream(socket.getOutputStream());

            os.write(msg.getBytes(ENCODING));
            os.flush();
            os.close();
        } catch (Exception e) {
            server.printError("processError: " + e.getMessage());
            server.printStackTrace(e);
        }
    }

    /**
     * Retrieves the thread name to be used  when
     * this object is the Runnable object of a Thread.
     *
     * @return the thread name to be used  when
     *      this object is the Runnable object of a Thread.
     */
    String getConnectionThreadName() {

        String s = toString();

        return s.substring(s.lastIndexOf('.') + 1);
    }
}
