package org.quartz;

import java.util.Collection;

/**
 * Provides a mechanism for obtaining client-usable handles to <code>Scheduler</code>
 * instances.
 *
 * @author James House
 * @see Scheduler
 * @see org.quartz.impl.StdSchedulerFactory
 */
public interface SchedulerFactory {


    /**
     * <p>
     * Returns a client-usable handle to a <code>Scheduler</code>.
     *
     * @throws SchedulerException if there is a problem with the underlying <code>Scheduler</code>.
     */
    Scheduler getScheduler() throws SchedulerException;

    /**
     * <p>
     * Returns a handle to the Scheduler with the given name, if it exists.
     * <p>
     */
    Scheduler getScheduler(String schedName) throws SchedulerException;

    /**
     * <p>
     * Returns handles to all known Schedulers (made by any SchedulerFactory
     * within this jvm.).
     * </p>
     */
    Collection<Scheduler> getAllSchedulers() throws SchedulerException;

}
