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
 *  All ServerConnection objects are listed in a Set in server
 *  and removed by this class when closed.<p>
 *
 *  When the datbase or server is shutdown, the signalClose() method is called
 *  for all current ServerConnection instances. This will call the private
 *  close() method unless the ServerConnection thread itself has caused the
 *  shutdown. In this case, the keepAlive flag is set to false, allowing the
 *  thread to terminate once it has returned the result of the operation to
 *  the client.
 *  (fredt@users)<p>
 *
 * @version 1.7.2
 */
class ServerConnection implements Runnable {

    boolean                 keepAlive;
    private String          user;
    int                     dbID;
    private Session         session;
    private Socket          socket;
    private Server          server;
    private DataInputStream dataInput;
    private OutputStream    dataOutput;
    private static int      mCurrentThread = 0;
    private int             mThread;
    static final int        BUFFER_SIZE = 0x1000;
    final byte[]            mainBuffer  = new byte[BUFFER_SIZE];
    BinaryServerRowOutput   rowOut = new BinaryServerRowOutput(BUFFER_SIZE);
    BinaryServerRowInput    rowIn       = new BinaryServerRowInput(rowOut);
    Thread                  runnerThread;

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

    void signalClose() {

        keepAlive = false;

        if (!runnerThread.equals(Thread.currentThread())) {
            close();
        }
    }

    private void close() {

        if (session != null) {
            session.disconnect();
        }

        session = null;

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
    private void init() {

        runnerThread = Thread.currentThread();
        keepAlive    = true;

        try {
            socket.setTcpNoDelay(true);

            dataInput = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
            dataOutput = new BufferedOutputStream(socket.getOutputStream());

            Result resultIn = Result.read(rowIn, dataInput);
            Result resultOut;

            try {
                int dbIndex = ArrayUtil.find(server.dbAlias,
                                             resultIn.subSubString);

                dbID = server.dbID[dbIndex];

                server.trace(mThread + ":trying to connect user " + user);

                session = DatabaseManager.newSession(dbID,
                                                     resultIn.getMainString(),
                                                     resultIn.getSubString());
                resultOut = new Result(ResultConstants.UPDATECOUNT);
                resultOut.databaseID = session.getDatabase().databaseID;
                resultOut.sessionID  = session.getId();
            } catch (HsqlException e) {
                session   = null;
                resultOut = new Result(e, null);
            } catch (ArrayIndexOutOfBoundsException e) {
                session = null;
                resultOut = new Result(
                    Trace.getError(Trace.DATABASE_NOT_EXISTS, null),
                    resultIn.subSubString);
            }

            Result.write(resultOut, rowOut, dataOutput);

            return;
        } catch (Exception e) {
            server.trace(mThread + ":couldn't connect " + user);

            if (session != null) {
                session.disconnect();
            }
        }

        close();
    }

    /**
     * Method declaration
     *
     */
    public void run() {

        init();

        if (session != null) {
            try {
                while (keepAlive) {
                    Result resultIn = Result.read(rowIn, dataInput);

                    server.traceRequest(mThread, resultIn);

                    Result resultOut = session.execute(resultIn);

                    Result.write(resultOut, rowOut, dataOutput);
                    rowOut.setBuffer(mainBuffer);
                    rowIn.resetRow(mainBuffer.length);
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

    String getConnectionThreadName() {

        String s = toString();

        return s.substring(s.lastIndexOf('.') + 1);
    }
}
