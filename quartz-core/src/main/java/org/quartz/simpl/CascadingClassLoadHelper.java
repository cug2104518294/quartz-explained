package org.quartz.simpl;

import org.quartz.spi.ClassLoadHelper;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <code>ClassLoadHelper</code> uses all of the <code>ClassLoadHelper</code>
 * types that are found in this package in its attempts to load a class, when
 * one scheme is found to work, it is promoted to the scheme that will be used
 * first the next time a class is loaded (in order to improve performance).
 *
 * <p>
 * This approach is used because of the wide variance in class loader behavior
 * between the various environments in which Quartz runs (e.g. disparate
 * application servers, stand-alone, mobile devices, etc.).  Because of this
 * disparity, Quartz ran into difficulty with a one class-load style fits-all
 * design.  Thus, this class loader finds the approach that works, then
 * 'remembers' it.
 * </p>
 *
 * <p>
 * 使用ClassLoadHelper的所有子类（都是class loader）去尝试加载一个类，并建立一个mapping，以后加载这个类时都优先使用该ClassLoader。
 * </p>
 *
 * @author jhouse
 * @author pl47ypus
 * @see org.quartz.spi.ClassLoadHelper
 * @see org.quartz.simpl.LoadingLoaderClassLoadHelper
 * @see org.quartz.simpl.SimpleClassLoadHelper
 * @see org.quartz.simpl.ThreadContextClassLoadHelper
 * @see org.quartz.simpl.InitThreadContextClassLoadHelper
 */
public class CascadingClassLoadHelper implements ClassLoadHelper {


    private LinkedList<ClassLoadHelper> loadHelpers;

    private ClassLoadHelper bestCandidate;

    /**
     * Called to give the ClassLoadHelper a chance to initialize itself,
     * including the opportunity to "steal" the class loader off of the calling
     * thread, which is the thread that is initializing Quartz.
     */
    @Override
    public void initialize() {
        loadHelpers = new LinkedList<>();
        loadHelpers.add(new LoadingLoaderClassLoadHelper());
        loadHelpers.add(new SimpleClassLoadHelper());
        loadHelpers.add(new ThreadContextClassLoadHelper());
        loadHelpers.add(new InitThreadContextClassLoadHelper());

        for (ClassLoadHelper loadHelper : loadHelpers) {
            loadHelper.initialize();
        }
    }

    /**
     * Return the class with the given name.
     * <p>
     * 遍历每一个class loader，去尝试进行类加载，如果加载成功，则设置为候选
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if (bestCandidate != null) {
            try {
                return bestCandidate.loadClass(name);
            } catch (Throwable t) {
                bestCandidate = null;
            }
        }

        Throwable throwable = null;
        Class<?> clazz = null;
        ClassLoadHelper loadHelper = null;

        Iterator<ClassLoadHelper> iter = loadHelpers.iterator();
        while (iter.hasNext()) {
            loadHelper = iter.next();

            try {
                clazz = loadHelper.loadClass(name);
                break;
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (clazz == null) {
            if (throwable instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) throwable;
            } else {
                throw new ClassNotFoundException(String.format("Unable to load class %s by any known loaders.", name), throwable);
            }
        }

        bestCandidate = loadHelper;

        return clazz;
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

        URL result = null;

        if (bestCandidate != null) {
            result = bestCandidate.getResource(name);
            if (result == null) {
                bestCandidate = null;
            } else {
                return result;
            }
        }

        ClassLoadHelper loadHelper = null;

        Iterator<ClassLoadHelper> iter = loadHelpers.iterator();
        while (iter.hasNext()) {
            loadHelper = iter.next();

            result = loadHelper.getResource(name);
            if (result != null) {
                break;
            }
        }

        bestCandidate = loadHelper;
        return result;
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

        InputStream result = null;

        if (bestCandidate != null) {
            result = bestCandidate.getResourceAsStream(name);
            if (result == null) {
                bestCandidate = null;
            } else {
                return result;
            }
        }

        ClassLoadHelper loadHelper = null;

        Iterator<ClassLoadHelper> iter = loadHelpers.iterator();
        while (iter.hasNext()) {
            loadHelper = iter.next();

            result = loadHelper.getResourceAsStream(name);
            if (result != null) {
                break;
            }
        }

        bestCandidate = loadHelper;
        return result;
    }

    /**
     * Enable sharing of the "best" class-loader with 3rd party.
     *
     * @return the class-loader user be the helper.
     */
    @Override
    public ClassLoader getClassLoader() {
        return (this.bestCandidate == null) ?
                Thread.currentThread().getContextClassLoader() :
                this.bestCandidate.getClassLoader();
    }

}
