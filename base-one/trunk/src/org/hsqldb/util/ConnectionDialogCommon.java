/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2004, The HSQL Development Group
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


package org.hsqldb.util;

import java.io.*;
import java.util.Vector;

import org.hsqldb.lib.HashMappedList;

// sqlbob@users 20020407 - patch 1.7.0 - reengineering
// fredt@users - 20040508 - modified patch by lonbinder@users for saving settings

/**
 * Common code in the Swing and AWT versions of ConnectionDialog
 * @version 1.7.2
 */
class ConnectionDialogCommon {

    private static String       connTypes[][];
    private static final String sJDBCTypes[][] = {
        {
            "HSQL Database Engine In-Memory", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:mem:."
        }, {
            "HSQL Database Engine Standalone", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:file:\u00ABdatabase/path?\u00BB"
        }, {
            "HSQL Database Engine Server", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:hsql://localhost/"
        }, {
            "HSQL Database Engine WebServer", "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:http://\u00ABhostname/?\u00BB"
        }, {
            "JDBC-ODBC Bridge from Sun", "sun.jdbc.odbc.JdbcOdbcDriver",
            "jdbc:odbc:\u00ABdatabase?\u00BB"
        }, {
            "Cloudscape RMI", "RmiJdbc.RJDriver",
            "jdbc:rmi://\u00ABhost?\u00BB:1099/jdbc:cloudscape:"
            + "\u00ABdatabase?\u00BB;create=true"
        }, {
            "IBM DB2", "COM.ibm.db2.jdbc.app.DB2Driver",
            "jdbc:db2:\u00ABdatabase?\u00BB"
        }, {
            "IBM DB2 (thin)", "COM.ibm.db2.jdbc.net.DB2Driver",
            "jdbc:db2://\u00ABhost?\u00BB:6789/\u00ABdatabase?\u00BB"
        }, {
            "Informix", "com.informix.jdbc.IfxDriver",
            "jdbc:informix-sqli://\u00ABhost?\u00BB:1533/\u00ABdatabase?\u00BB:"
            + "INFORMIXSERVER=\u00ABserver?\u00BB"
        }, {
            "InstantDb", "jdbc.idbDriver",
            "jdbc:idb:\u00ABdatabase?\u00BB.prp"
        }, {
            "MM.MySQL", "org.gjt.mm.mysql.Driver",
            "jdbc:mysql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "Oracle", "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:oci8:@\u00ABdatabase?\u00BB"
        }, {
            "Oracle (thin)", "oracle.jdbc.driver.OracleDriver",
            "jdbc:oracle:thin:@\u00ABhost?\u00BB:1521:\u00ABdatabase?\u00BB"
        }, {
            "PointBase", "com.pointbase.jdbc.jdbcUniversalDriver",
            "jdbc:pointbase://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "PostgreSQL", "org.postgresql.Driver",
            "jdbc:postgresql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }, {
            "PostgreSQL v6.5", "postgresql.Driver",
            "jdbc:postgresql://\u00ABhost?\u00BB/\u00ABdatabase?\u00BB"
        }
    };

    static String[][] getTypes() {

        if (connTypes == null) {

            // Pluggable connection types:
            Vector plugTypes = new Vector();

            try {
                plugTypes = (Vector) Class.forName(
                    System.getProperty(
                        "org.hsqldb.util.ConnectionTypeClass")).newInstance();
            } catch (Exception e) {
                ;
            }

            connTypes =
                new String[(plugTypes.size() / 3) + sJDBCTypes.length][3];

            int i = 0;

            for (int j = 0; j < plugTypes.size(); i++) {
                connTypes[i]    = new String[3];
                connTypes[i][0] = plugTypes.elementAt(j++).toString();
                connTypes[i][1] = plugTypes.elementAt(j++).toString();
                connTypes[i][2] = plugTypes.elementAt(j++).toString();
            }

            for (int j = 0; j < sJDBCTypes.length; i++, j++) {
                connTypes[i]    = new String[3];
                connTypes[i][0] = sJDBCTypes[j][0];
                connTypes[i][1] = sJDBCTypes[j][1];
                connTypes[i][2] = sJDBCTypes[j][2];
            }
        }

        return (connTypes);
    }

    private static final String fileName       = "hsqlprefs.dat";
    private static File         recentSettings = null;

    static HashMappedList loadRecentConnectionSettings() throws IOException {

        HashMappedList list = new HashMappedList();

        list.add(emptySetting.getName(), emptySetting);

        try {
            if (recentSettings == null) {
                String dir = getTempDir();

                if (dir == null) {
                    return list;
                }

                recentSettings = new File(dir, fileName);

                if (!recentSettings.exists()) {
                    recentSettings.createNewFile();

                    return list;
                }
            }
        } catch (Throwable e) {
            return list;
        }

        FileInputStream   in        = new FileInputStream(recentSettings);
        ObjectInputStream objStream = null;

        try {
            objStream = new ObjectInputStream(in);

            list.clear();

            while (true) {
                ConnectionSetting setting =
                    (ConnectionSetting) objStream.readObject();

                list.add(setting.getName(), setting);
            }
        } catch (EOFException eof) {

            // reached end of file -- this is not clean but it works
        } catch (ClassNotFoundException cnfe) {
            throw (IOException) new IOException("Unrecognized class type "
                                                + cnfe.getMessage());
        } catch (ClassCastException cce) {
            throw (IOException) new IOException("Unrecognized class type "
                                                + cce.getMessage());
        } catch (Throwable t) {}
        finally {
            if (objStream != null) {
                objStream.close();
            }

            in.close();
        }

        if (list.size() == 0) {
            list.add(emptySetting.getName(), emptySetting);
        }

        return list;
    }

    static ConnectionSetting emptySetting =
        new ConnectionSetting("Recent settings...", null, null, null, null);

    /**
     * Adds the new settings name if it does not nexist, or overwrites the old one.
     */
    static void addToRecentConnectionSettings(HashMappedList settings,
            ConnectionSetting newSetting) throws IOException {
        settings.put(newSetting.getName(), newSetting);
        ConnectionDialogCommon.storeRecentConnectionSettings(settings);
    }

    /**
     * Here's a non-secure method of storing recent connection settings.
     *
     * @param settings ConnectionSetting[]
     * @throw IOException if something goes wrong while writing
     */
    private static void storeRecentConnectionSettings(
            HashMappedList settings) {

        try {
            if (recentSettings == null) {
                String dir = getTempDir();

                if (dir == null) {
                    return;
                }

                recentSettings = new File(dir, fileName);

                if (!recentSettings.exists()) {
                    recentSettings.createNewFile();
                }
            }

            if (settings == null || settings.size() == 0) {
                return;
            }

            // setup a stream to a physical file on the filesystem
            FileOutputStream   out = new FileOutputStream(recentSettings);
            ObjectOutputStream objStream = new ObjectOutputStream(out);

            for (int i = 0; i < settings.size(); i++) {
                objStream.writeObject(settings.get(i));
            }

            objStream.flush();
            objStream.close();
            out.close();
        } catch (Throwable t) {}
    }

    /**
     * Removes the recent connection settings file store.
     */
    static void deleteRecentConnectionSettings() {

        try {
            if (recentSettings == null) {
                String dir = getTempDir();

                if (dir == null) {
                    return;
                }

                recentSettings = new File(dir, fileName);
            }

            if (!recentSettings.exists()) {
                recentSettings = null;

                return;
            }

            recentSettings.delete();

            recentSettings = null;
        } catch (Throwable t) {}
    }

    private static String tmpdir = null;

    private static String getTempDir() {

        if (tmpdir == null) {
            try {
                Class.forName("sun.security.action.GetPropertyAction");

                sun.security.action.GetPropertyAction a =
                    new sun.security.action.GetPropertyAction(
                        "java.io.tmpdir");

                tmpdir =
                    ((String) java.security.AccessController.doPrivileged(a));
            } catch (Exception e) {}
        }

        return tmpdir;
    }
}
