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
import java.io.IOException;
import java.net.Socket;

// fredt@users 20020215 - patch 461556 by paul-h@users - server factory
// fredt@users 20020424 - patch 1.7.0 by fredt - shutdown without exit
// fredt@users 20021002 - patch 1.7.1 by fredt - changed notification method

/**
 *  All ServerConnection objects are listed in a Vector in mServer
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
class ServerConnection implements Runnable, ServerConstants {

    private String           user;
    private Session          session;
    private Socket           mSocket;
    private Server           mServer;
    private DataInputStream  mInput;
    private DataOutputStream mOutput;
    private static int       mCurrentThread = 0;
    private int              mThread;

    /**
     *
     * @param socket
     * @param server
     */
    ServerConnection(Socket socket, Server server) {

        mSocket = socket;
        mServer = server;

        synchronized (ServerConnection.class) {
            mThread = mCurrentThread++;
        }

        mServer.serverConnList.addElement(this);
    }

    void close() {

        // fredt@user - closing the socket is to stop this thread
        try {
            mSocket.close();
        } catch (IOException e) {}

        mServer.serverConnList.removeElement(this);
        mServer.notify(SC_CONNECTION_CLOSED);
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    private Session init() {

        try {
            mSocket.setTcpNoDelay(true);

            mInput = new DataInputStream(
                new BufferedInputStream(mSocket.getInputStream()));
            mOutput = new DataOutputStream(
                new BufferedOutputStream(mSocket.getOutputStream()));
            user = mInput.readUTF();

            String  password = mInput.readUTF();
            Session c;

            try {
                mServer.trace(mThread + ":trying to connect user " + user);

                return mServer.mDatabase.connect(user, password);
            } catch (HsqlException e) {
                write(new Result(e.getMessage(),
                                 e.getSQLState(), e.getErrorCode()).getBytes());
            }
        } catch (Exception e) {
            mServer.trace(mThread + ":couldn't connect " + user);
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

// fredt@users 20011220 - patch 448121 by sma@users - large binary values
                    byte[] bytes = new byte[mInput.readInt()];

                    mInput.readFully(bytes);

                    String sql = new String(bytes, "utf-8");

                    mServer.trace(mThread + ":" + sql);

                    if (sql == null) {
                        break;
                    }

                    // fredt - to optimise by reusing a BinaryServerRowOutput object
                    write(mServer.mDatabase.execute(sql, session).getBytes());

                    if (mServer.mDatabase.isShutdown()) {
                        break;
                    }
                }
            } catch (IOException e) {
                mServer.trace(mThread + ":disconnected " + user);

                // fredt - after the client abrubtly drops clear the session
                session.disconnect();
            } catch (HsqlException e) {

                // is thrown by Result.getBytes()
                String s = e.getMessage();

                e.printStackTrace();
            }

            close();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param b
     *
     * @throws IOException
     */

    // fredt - todo - rewrite for reusing the output buffer
    void write(DatabaseRowOutputInterface binImage) throws IOException {

        mOutput.writeInt(binImage.getOutputStream().size());
        mOutput.write(binImage.getOutputStream().getBuffer(), 0,
                      binImage.getOutputStream().size());
        mOutput.flush();
    }

    void write(byte b[]) throws IOException {

        mOutput.writeInt(b.length);
        mOutput.write(b);
        mOutput.flush();
    }
}
