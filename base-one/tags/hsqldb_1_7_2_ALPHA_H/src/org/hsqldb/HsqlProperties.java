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


package org.hsqldb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;
import org.hsqldb.lib.java.javaSystem;
import org.hsqldb.lib.FileUtil;


/**
 * Wrapper for java.util.Properties to limit values to String objects and
 * allow saving and loading.
 *
 * @author fredt@users
 * @verison 1.7.0
 */
public class HsqlProperties {

    private static Method savePropsMethod = null;

    static {
        try {
            savePropsMethod = java.util.Properties.class.getMethod("store",
                    new Class[] {
                OutputStream.class, String.class
            });
        } catch (NoSuchMethodException e) {}
        catch (SecurityException e) {}
    }

    protected String     fileName;
    protected Properties stringProps;;

    public HsqlProperties() {
        stringProps = new Properties();
        fileName    = null;
    }

    public HsqlProperties(String name) {
        stringProps = new Properties();
        fileName    = name;
    }

    public HsqlProperties(Properties props) {
        stringProps = props;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public String setProperty(String key, int value) {
        return (String) stringProps.put(key, Integer.toString(value));
    }

    public String setProperty(String key, boolean value) {
        return (String) stringProps.put(key, String.valueOf(value));
    }

    public String setProperty(String key, String value) {
        return (String) stringProps.put(key, value);
    }

    public String setPropertyIfNotExists(String key, String value) {

        value = stringProps.getProperty(key, value);

        return (String) stringProps.put(key, value);
    }

    public String getProperty(String key) {
        return stringProps.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return stringProps.getProperty(key, defaultValue);
    }

    public int getIntegerProperty(String key, int defaultValue) {

        String prop = getProperty(key);

        try {
            defaultValue = Integer.parseInt(prop);
        } catch (NumberFormatException e) {}

        return defaultValue;
    }

    public boolean isPropertyTrue(String key) {
        return isPropertyTrue(key, false);
    }

    public boolean isPropertyTrue(String key, boolean defaultValue) {

        String value = stringProps.getProperty(key, defaultValue ? "true"
                                                                 : "false");

        return Boolean.valueOf(value).booleanValue();
    }

    public void removeProperty(String key) {
        stringProps.remove(key);
    }

    public static HsqlProperties argArrayToProps(String[] arg, String type) {

        HsqlProperties props = new HsqlProperties();

        for (int i = 0; i < arg.length - 1; i++) {
            String p = arg[i];

            if ((p.charAt(0) == '-') && (!p.startsWith("-?"))) {
                props.setProperty(type + "." + p.substring(1), arg[i + 1]);

                i++;
            }
        }

        return props;
    }

    public void addProperties(HsqlProperties props) {

        Enumeration keys = props.stringProps.propertyNames();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            this.stringProps.put(key, props.stringProps.get(key));
        }
    }

    public boolean checkFileExists() {

        if (fileName == null || fileName.length() == 0) {
            return false;
        }

        return FileUtil.exists(fileName + ".properties");
    }

    public void load() throws Exception {

        if (fileName == null || fileName.length() == 0) {
            throw new java.io.FileNotFoundException(
                "properties name is null or empty");
        }

        FileInputStream fis = null;

        try {
            File f = new File(fileName + ".properties");

            fis = new FileInputStream(f);

            stringProps.load(fis);
            fis.close();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    public void save() throws Exception {

        if (fileName == null || fileName.length() == 0) {
            throw new java.io.FileNotFoundException(
                "properties name is null or empty");
        }

        String filestring = fileName + ".properties";
        File   f          = new File(filestring);
        String parent     = f.getParent();

        if (parent != null) {
            new File(parent).mkdirs();
        } else {

            // workaround for jdk 1.1 bug (returns null when there is a parent)
            parent = f.getPath();

            int index = parent.lastIndexOf('/');

            if (index > 0) {
                parent = parent.substring(0, index);

                new File(parent).mkdirs();
            }
        }

        FileOutputStream fos = new FileOutputStream(f);

        if (savePropsMethod == null) {
            stringProps.save(fos, "HSQL database");
        } else {
            try {
                savePropsMethod.invoke(stringProps, new Object[] {
                    fos, "HSQL database"
                });
            } catch (java.lang.reflect.InvocationTargetException e) {}
            catch (IllegalAccessException e) {}
        }

        fos.close();
    }
}
