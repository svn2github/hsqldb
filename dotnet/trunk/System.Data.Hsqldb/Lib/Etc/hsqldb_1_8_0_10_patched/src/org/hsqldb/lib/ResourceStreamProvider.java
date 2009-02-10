/*
 * ResourceStreamProvider.java
 *
 * Created on April 17, 2007, 5:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.hsqldb.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.hsqldb.HsqlException;
import org.hsqldb.Trace;
import org.hsqldb.persist.Logger;

/**
 *
 * @author boucherb@users
 */
public class ResourceStreamProvider {
    
    private static ClassLoader loader;
    
    /** Creates a new instance of ResourceStreamProvider */
    private ResourceStreamProvider() {
    }
    
    public static synchronized void setLoader(ClassLoader loader)
    {
        ResourceStreamProvider.loader = loader;
    }
    
    public static synchronized ClassLoader getLoader()
    {
        return ResourceStreamProvider.loader;
    }
    
    public static boolean exists(String resource)
    {
        ClassLoader loader = ResourceStreamProvider.getLoader();
        
        URL url = null;
        
        if (loader == null)
        {
            url = Logger.class.getResource(resource);            
        }
        else
        {
            url = loader.getResource(resource);
            
            if (url == null)
            {
                url = Logger.class.getResource(resource);
            }
        }  
        
        return !(url == null ||  "file".equals(url.getProtocol()));
    }
    
    public static InputStream getResourceAsStream(String resource) 
    throws IOException
    {
        ClassLoader loader = ResourceStreamProvider.getLoader();
        
        URL url = null;
        
        if (loader == null)
        {
            url = Logger.class.getResource(resource);            
        }
        else
        {
            url = loader.getResource(resource);
            
            if (url == null)
            {
                url = Logger.class.getResource(resource);
            }
        }
        
        if (url == null)
        {
           throw new IOException(
                    "Missing resource: " + resource);
        }
        
        String protocol = url.getProtocol();
        
        if ("file".equals(protocol))
        {
            throw new IOException(
                    "Wrong protocol [file] for resource : " + resource);
        }
        
       return url.openStream();
    }
    
}
