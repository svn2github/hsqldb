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


package org.hsqldb.lib;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides a static utility interface to an MD5 digest algorithm
 * obtained through the java.security.MessageDigest spi
 */
public final class MD5 {

    /**
     * The jce MD5 message digest generator.
     */
    private static MessageDigest md5;

    /**
     * Retrieves a hexidecimal character sequence representing the MD5
     * digest of the specified character sequence, using the specified
     * encoding to first convert the character sequence into a byte sequence.
     * If the specified encoding is null, then ISO-8859-1 is assumed
     *
     * @param string the string to encode.
     * @param encoding the encoding used to convert the string into the
     *      byte sequence to submit for MD5 digest
     * @return a hexidecimal character sequence representing the MD5
     *      digest of the specified string
     * @throws UnsupportedOperationException if an MD5 digest
     *       algorithm is not available through the
     *       java.security.MessageDigest spi
     */
    public static final String encode(String string,
                                      String encoding)
                                      throws UnsupportedOperationException {
        return StringConverter.byteToHex(digest(string, encoding));
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the
     * specified character sequence, using the specified encoding to
     * first convert the character sequence into a byte sequence.
     * If the specified encoding is not available, the default encoding
     * is used.  If the specified encoding is null, then ISO-8859-1 is
     * assumed.
     *
     * @param string the string.
     * @param encoding the character encoding.
     * @return the digest as an array of 16 bytes.
     * @throws UnsupportedOperationException if an MD5 digest
     *      algorithm is not available through the
     *      java.security.MessageDigest spi
     */
    public static byte[] digest(String string,
                                String encoding)
                                throws UnsupportedOperationException {

        byte[] data;

        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        try {
            data = string.getBytes(encoding);
        } catch (UnsupportedEncodingException x) {
            data = string.getBytes();
        }

        return digest(data);
    }

    /**
     * Retrieves a byte sequence representing the MD5 digest of the
     * specified the specified byte sequence.
     *
     * @param data the data to digest.
     * @return the MD5 digest as an array of 16 bytes.
     * @throws UnsupportedOperationException if an MD5 digest
     *       algorithm is not available through the
     *       java.security.MessageDigest spi
     */
    public static final byte[] digest(byte[] data)
    throws UnsupportedOperationException {

        synchronized (MD5.class) {
            if (md5 == null) {
                try {
                    md5 = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new UnsupportedOperationException(e.toString());
                }
            }

            return md5.digest(data);
        }
    }
}
