/* Copyright (c) 2001-2002, The HSQL Development Group
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


package org.hsqldb.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.ValuePool;

/** A ResourceBundle helper class. <p>
 *
 * - Allows clients to get/set locale and get at localized resource bundles
 * in a resource path independent manner. <p>
 *
 * @author Campbell Boucher-Burnet, Camco & Associates Consulting
 * @version 1.0
 * @since HSQLDB 1.7.2
 */
public final class BundleHandler {

    /** Used to synchronize access */
    private static final Object     _lock            = new Object();
    /** The Locale used internally to fetch resource bundles. */
    private static Locale           _locale          = Locale.getDefault();
    /** Map:  Integer object handle => <code>ResourceBundle</code> object. */
    private static HsqlHashMap      _bundleHandleMap = new HsqlHashMap();
    /** List whose elements are <code>ResourceBundle</code> objects */
    private static HsqlArrayList    _bundleList      = new HsqlArrayList();
    /** The resource path prefix of the <code>ResourceBundle</code> objects
     * handled by this class.
     */
    private static final String     _prefix          = "org/hsqldb/resources/";

    /** Pure utility class: external construction disabled. */
    private BundleHandler() {}

    /**
     * Getter for property locale. <p>
     *
     * @return Value of property locale.
     */
    public static Locale getLocale() {
        return _locale;
    }

    /**
     * Setter for property locale. <p>
     *
     * @param l the new locale
     * @throws IllegalArgumentException when the new locale is null
     */
    public static void setLocale(Locale l) throws IllegalArgumentException {

        synchronized(_lock) {

            if (l == null) {
                throw new IllegalArgumentException("null locale");
            }

            _locale = l;
        }
    }

    /** Retrieves an <code>int</code> handle to the <code>ResourceBundle</code>
     * object corresponding to the specified name and current
     * <code>Locale</code>, using the specified <code>ClassLoader</code>. <p>
     *
     * @return <code>int</code> handle to the <code>ResourceBundle</code>
     *        object corresponding to the specified name and
     *        current <code>Locale</code>, or -1 if no such bundle
     *        can be found
     * @param cl The ClassLoader to use in the search
     * @param name of the desired bundle
     */
    public static int getBundleHandle(String name, ClassLoader cl) {

        Integer         bundleHandle;
        ResourceBundle  bundle;
        String          bundleName;
        String          bundleKey;

        synchronized(_lock) {

            bundleName      = _prefix + name;
            bundleKey       = _locale.toString() + bundleName;

            bundleHandle = (Integer) _bundleHandleMap.get(bundleKey);

            if (bundleHandle == null) {
                try {
                    bundle = (cl == null)
                    ? ResourceBundle.getBundle(bundleName, _locale)
                    : ResourceBundle.getBundle(bundleName, _locale, cl);
                    _bundleList.add(bundle);
                     bundleHandle = ValuePool.getInt(_bundleList.size()-1);
                    _bundleHandleMap.put(bundleKey, bundleHandle);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

            return bundleHandle == null ? -1 : bundleHandle.intValue();
        }
    }

    /** Retrieves, from the <code>ResourceBundle</code> object corresponding
     * to the specified handle, the <code>String</code> value corresponding
     * to the specified key.  <code>null</code> is retrieved if either there
     *  is no <code>ResourceBundle</code> object for the handle or there is no
     * <code>String</code> value for the specified key. <p>
     *
     * @param handle an <code>int</code> handle to a
     *      <code>ResourceBundle</code> object
     * @param key A <code>String</code> key to a <code>String</code> value
     * @return The String value correspoding to the specified handle and key.
     */
    public static String getString(int handle, String key) {

        synchronized(_lock) {

            if (handle < 0 || handle >= _bundleList.size() || key == null) {
                return null;
            }

            ResourceBundle bundle = (ResourceBundle) _bundleList.get(handle);
            String         s      = null;

            try {
                s = (bundle == null) ? null : bundle.getString(key);
            } catch (Exception e) {}

            return s;
        }
    }
}
