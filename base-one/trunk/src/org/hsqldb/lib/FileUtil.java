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
 * Copyright (c) 2001-2004, The HSQL Development Group
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


package org.hsqldb.lib;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//import java.util.zip.
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class FileUtil {

    // a new File("...")'s path is not canonicalized, only resolved
    // and normalized (e.g. redundant separator chars removed),
    // so as of JDK 1.4.2, this is a valid test for case insensitivity,
    // at least when it is assumed that we are dealing in a configuration
    // that only needs to consider the host platform's native file system,
    // even if, unlike for File.getCanonicalPath(), (new File("a")).exists() or 
    // (new File("A")).exits(), regardless of the hosting system's
    // file path case sensitivity policy.
    public static final boolean fsIsIgnoreCase =
        (new File("A")).equals(new File("a"));

    // posix separator normalized to File.separator?
    // CHECKME is this true for every file system under Java?
    public static final boolean fsNormalizesPosixSeparator =
        (new File("/")).getPath().endsWith(File.separator);
    private static final int COPY_BLOCK_SIZE = 1 << 16;

    public static void compressFile(String infilename,
                                    String outfilename) throws IOException {

        FileInputStream      in        = null;
        DeflaterOutputStream f         = null;
        boolean              completed = false;

        try {

            // if there is no file
            if (!(new File(infilename)).exists()) {
                return;
            }

            byte b[] = new byte[COPY_BLOCK_SIZE];

            in = new FileInputStream(infilename);
            f = new DeflaterOutputStream(new FileOutputStream(outfilename),
                                         new Deflater(Deflater.BEST_SPEED),
                                         COPY_BLOCK_SIZE);

            while (true) {
                int l = in.read(b, 0, COPY_BLOCK_SIZE);

                if (l == -1) {
                    break;
                }

                f.write(b, 0, l);
            }

            completed = true;
        } catch (Throwable e) {
            throw toIOException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (f != null) {
                    f.close();
                }

                if (!completed) {
                    delete(outfilename);
                }
            } catch (Throwable e) {
                throw toIOException(e);
            }
        }
    }

    public static void decompressFile(String infilename,
                                      String outfilename) throws IOException {

        InflaterInputStream f         = null;
        FileOutputStream    outstream = null;
        boolean             completed = false;

        try {
            if (!(new File(infilename)).exists()) {
                return;
            }

            f = new InflaterInputStream(new FileInputStream(infilename),
                                        new Inflater());
            outstream = new FileOutputStream(outfilename);

            byte b[] = new byte[COPY_BLOCK_SIZE];

            while (true) {
                int l = f.read(b, 0, COPY_BLOCK_SIZE);

                if (l == -1) {
                    break;
                }

                outstream.write(b, 0, l);
            }

            completed = true;
        } catch (Throwable e) {
            throw toIOException(e);
        } finally {
            try {
                if (f != null) {
                    f.close();
                }

                if (outstream != null) {
                    outstream.close();
                }

                if (!completed) {
                    delete(outfilename);
                }
            } catch (Throwable e) {
                throw toIOException(e);
            }
        }
    }

    /**
     * Delete the named file
     */
    static public void delete(String filename) throws IOException {

        try {
            (new File(filename)).delete();
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    /**
     * Return true or false based on whether the named file exists.
     */
    static public boolean exists(String filename) throws IOException {

        try {
            return (new File(filename)).exists();
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    /**
     * Rename the file with oldname to newname. Do nothing if the oldname
     * file does not exist. If a file named newname already exists, delete
     * it before ranaming.
     */
    static public void renameOverwrite(String oldname,
                                       String newname) throws IOException {

        try {
            if (exists(oldname)) {
                delete(newname);

                File file = new File(oldname);

                file.renameTo(new File(newname));
            }
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    static void printSystemOut(String message) {
        System.out.println(message);
    }

    static IOException toIOException(Throwable e) {

        if (e instanceof IOException) {
            return (IOException) e;
        } else {
            return new IOException(e.getMessage());
        }
    }

    /**
     * Retrieves the absolute path, given some path specification.
     *
     * @param path the path for which to retrieve the absolute path
     * @return the absolute path
     */
    public static String absolutePath(String path) {
        return (new File(path)).getAbsolutePath();
    }

    /**
     * Retrieves the absolute File, given some path specification.
     *
     * @param path the path for which to retrieve the absolute File
     * @return the absolute File
     */
    public static File absoluteFile(String path) {
        return (new File(path)).getAbsoluteFile();
    }

    /**
     * Retrieves the canonical file for the given file, in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the absolute File
     * @return the canonical File
     */
    public static File canonicalFile(File f) throws IOException {
        return new File(f.getCanonicalPath());
    }

    /**
     * Retrieves the canonical file for the given path, in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical File
     * @return the canonical File
     */
    public static File canonicalFile(String path) throws IOException {
        return new File(new File(path).getCanonicalPath());
    }

    /**
     * Retrieves the canonical path for the given File in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the canonical path
     * @return the canonical path
     */
    public static String canonicalPath(File f) throws IOException {
        return f.getCanonicalPath();
    }

    /**
     * Retrieves the canonical path for the given path in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical path
     * @return the canonical path
     */
    public static String canonicalPath(String path) throws IOException {
        return new File(path).getCanonicalPath();
    }

    /**
     * Retrieves the canonical path for the given path, or the absolute
     * path if attemting to retrieve the canonical path fails.
     *
     * @param path the path for which to retrieve the canonical or
     *      absolute path
     * @return the canonical or absolute path
     */
    public static String canonicalOrAbsolutePath(String path) {

        try {
            return canonicalPath(path);
        } catch (Exception e) {
            return absolutePath(path);
        }
    }
}
