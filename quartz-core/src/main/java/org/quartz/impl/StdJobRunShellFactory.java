package org.quartz.impl;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * <p>
 * Responsible for creating the instances of <code>{@link org.quartz.core.JobRunShell}</code>
 * to be used within the <class>{@link org.quartz.core.QuartzScheduler}
 * </code> instance.
 * </p>
 *
 * @author James House
 */
public class StdJobRunShellFactory implements JobRunShellFactory {

    private Scheduler scheduler;

    /**
     * <p>
     * Initialize the factory, providing a handle to the <code>Scheduler</code>
     * that should be made available within the <code>JobRunShell</code> and
     * the <code>JobExecutionContext</code> s within it.
     * </p>
     */
    @Override
    public void initialize(Scheduler sched) {
        this.scheduler = sched;
    }

    /**
     * <p>
     * Called by the <class>{@link org.quartz.core.QuartzSchedulerThread}
     * </code> to obtain instances of <code>
     * {@link org.quartz.core.JobRunShell}</code>.
     * </p>
     */
    @Override
    public JobRunShell createJobRunShell(TriggerFiredBundle bndle) throws SchedulerException {
        return new JobRunShell(scheduler, bndle);
    }
}
