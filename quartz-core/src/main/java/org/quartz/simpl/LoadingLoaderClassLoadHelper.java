package org.quartz.simpl;

import org.quartz.spi.ClassLoadHelper;

import java.io.InputStream;
import java.net.URL;

/**
 * A <code>ClassLoadHelper</code> that uses either the loader of it's own
 * class (<code>this.getClass().getClassLoader().loadClass( .. )</code>).
 *
 * @author jhouse
 * @author pl47ypus
 * @see org.quartz.spi.ClassLoadHelper
 * @see org.quartz.simpl.InitThreadContextClassLoadHelper
 * @see org.quartz.simpl.SimpleClassLoadHelper
 * @see org.quartz.simpl.CascadingClassLoadHelper
 */
public class LoadingLoaderClassLoadHelper implements ClassLoadHelper {

    /**
     * Called to give the ClassLoadHelper a chance to initialize itself,
     * including the opportunity to "steal" the class loader off of the calling
     * thread, which is the thread that is initializing Quartz.
     */
    @Override
    public void initialize() {
    }

    /**
     * Return the class with the given name.
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return getClassLoader().loadClass(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(String name, Class<T> clazz)
            throws ClassNotFoundException {
        return (Class<? extends T>) loadClass(name);
    }

    /**
     * Finds a resource with a given name. This method returns null if no
     * resource with this name is found.
     *
     * @param name name of the desired resource
     * @return a java.net.URL object
     */
    @Override
    public URL getResource(String name) {
        return getClassLoader().getResource(name);
    }

    /**
     * Finds a resource with a given name. This method returns null if no
     * resource with this name is found.
     *
     * @param name name of the desired resource
     * @return a java.io.InputStream object
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        return getClassLoader().getResourceAsStream(name);
    }

    /**
     * Enable sharing of the class-loader with 3rd party.
     *
     * @return the class-loader user be the helper.
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }
}
