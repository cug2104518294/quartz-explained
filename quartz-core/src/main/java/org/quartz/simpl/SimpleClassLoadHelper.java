package org.quartz.simpl;

import org.quartz.spi.ClassLoadHelper;

import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * A <code>ClassLoadHelper</code> that simply calls <code>Class.forName(..)</code>.
 *
 * @see org.quartz.spi.ClassLoadHelper
 * @see org.quartz.simpl.ThreadContextClassLoadHelper
 * @see org.quartz.simpl.CascadingClassLoadHelper
 * @see org.quartz.simpl.LoadingLoaderClassLoadHelper
 */
public class SimpleClassLoadHelper implements ClassLoadHelper {

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
        return Class.forName(name);
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
        // To follow the same behavior of Class.forName(...) I had to play
        // dirty (Supported by Sun, IBM & BEA JVMs)
        try {
            // Get a reference to this class' class-loader
            ClassLoader cl = this.getClass().getClassLoader();
            // Create a method instance representing the protected
            // getCallerClassLoader method of class ClassLoader
            Method mthd = ClassLoader.class.getDeclaredMethod(
                    "getCallerClassLoader", new Class<?>[0]);
            // Make the method accessible.
            AccessibleObject.setAccessible(new AccessibleObject[]{mthd}, true);
            // Try to get the caller's class-loader
            return (ClassLoader) mthd.invoke(cl, new Object[0]);
        } catch (Throwable all) {
            // Use this class' class-loader
            return this.getClass().getClassLoader();
        }
    }

}
