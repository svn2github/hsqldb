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
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import org.hsqldb.lib.ArrayUtil;

// fredt@users 20020215 - patch 461556 by paul-h@users - server factory
// fredt@users 20020424 - patch 1.7.0 by fredt - shutdown without exit
// fredt@users 20021002 - patch 1.7.1 by fredt - changed notification method
// fredt@users 20030618 - patch 1.7.2 by fredt - changed read -write

/**
 *  All ServerConnection objects are listed in a Vector in server
 *  and removed when closed.<p>
 *
 *  When a connection is dropped or closed the Server.notify() method is
 *  called. Upon notification, tf the DB is shutdown as a result of SHUTDOWN,
 *  the server stops all
 *  ServerConnection threads. At this point, only the skeletal Server
 *  object remains and everything else will be garbage collected.
 *  (fredt@users)<p>
 *
 * @version 1.7.2
 */
class ServerConnection implements Runnable {

    private String          user;
    int                     dbIndex;
    private Session         session;
    private Socket          socket;
    private Server          server;
    private DataInputStream dataInput;
    private OutputStream    dataOutput;
    private static int      mCurrentThread = 0;
    private int             mThread;
    static final int        bufferSize = 256;
    BinaryServerRowOutput   rowOut = new BinaryServerRowOutput(bufferSize);
    BinaryServerRowInput    rowIn      = new BinaryServerRowInput(rowOut);

    /**
     *
     * @param socket
     * @param server
     */
    ServerConnection(Socket socket, Server server) {

        this.socket = socket;
        this.server = server;

        synchronized (ServerConnection.class) {
            mThread = mCurrentThread++;
        }

        synchronized (server.serverConnSet) {
            server.serverConnSet.add(this);
        }
    }

    void close() {

        if (session != null) {
            session.disconnect();
        }

        // fredt@user - closing the socket is to stop this thread
        try {
            socket.close();
        } catch (IOException e) {}

        synchronized (server.serverConnSet) {
            server.serverConnSet.remove(this);
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    private Session init() {

        Session c = null;

        try {
            socket.setTcpNoDelay(true);

            dataInput = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
            dataOutput = new BufferedOutputStream(socket.getOutputStream());

            Result resultIn = HSQLClientConnection.read(rowIn, dataInput);
            Result resultOut;

            try {
                dbIndex = ArrayUtil.find(server.dbAlias,
                                         resultIn.subSubString);

                server.trace(mThread + ":trying to connect user " + user);

                c = DatabaseManager.newSession(server.dbType[dbIndex],
                                               server.dbPath[dbIndex],
                                               resultIn.getMainString(),
                                               resultIn.getSubString(), true);
                resultOut = new Result(ResultConstants.UPDATECOUNT);
            } catch (HsqlException e) {
                resultOut = new Result(e, null);
            } catch (ArrayIndexOutOfBoundsException e) {
                resultOut = new Result(
                    Trace.getError(Trace.DATABASE_NOT_EXISTS, null),
                    resultIn.subSubString);
            }

            HSQLClientConnection.write(resultOut, rowOut, dataOutput);

            return c;
        } catch (Exception e) {
            server.trace(mThread + ":couldn't connect " + user);

            if (c != null) {
                c.disconnect();
            }
        }

        close();

        return null;
    }

    /**
     * Method declaration
     *
     */
    public void run() {

        session = init();

        if (session != null) {
            try {
                while (true) {
                    Result resultIn = HSQLClientConnection.read(rowIn,
                        dataInput);

                    server.trace(mThread + ":" + resultIn.getMainString());

                    Result resultOut = session.execute(resultIn);

                    HSQLClientConnection.write(resultOut, rowOut, dataOutput);
/*
                    if (server.mDatabase.isShutdown()) {
                        break;
                    }
*/
                }
            } catch (IOException e) {

                // fredt - is thrown when connection drops
                server.trace(mThread + ":disconnected " + user);
            } catch (HsqlException e) {

                // fredt - is thrown while constructing the result
                String s = e.getMessage();

                e.printStackTrace();
            }

            close();
        }
    }
}
