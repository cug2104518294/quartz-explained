package org.quartz.spi;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * An interface to be used by <code>JobStore</code> instances in order to
 * communicate signals back to the <code>QuartzScheduler</code>.
 */
public interface SchedulerSignaler {

    void notifyTriggerListenersMisfired(Trigger trigger);

    void notifySchedulerListenersFinalized(Trigger trigger);

    void notifySchedulerListenersJobDeleted(JobKey jobKey);

    void signalSchedulingChange(long candidateNewNextFireTime);

    void notifySchedulerListenersError(String string, SchedulerException jpe);
}
