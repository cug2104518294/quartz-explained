package org.quartz.core;

import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.SchedulerSignaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interface to be used by <code>JobStore</code> instances in order to
 * communicate signals back to the <code>QuartzScheduler</code>.
 * <p>
 * JobStore对QuartzScheduler的通知
 */
public class SchedulerSignalerImpl implements SchedulerSignaler {

    Logger log = LoggerFactory.getLogger(SchedulerSignalerImpl.class);

    protected QuartzScheduler sched;

    protected QuartzSchedulerThread schedThread;

    public SchedulerSignalerImpl(QuartzScheduler sched, QuartzSchedulerThread schedThread) {
        this.sched = sched;
        this.schedThread = schedThread;

        log.info("Initialized Scheduler Signaller of type: " + getClass());
    }

    @Override
    public void notifyTriggerListenersMisfired(Trigger trigger) {
        try {
            sched.notifyTriggerListenersMisfired(trigger);
        } catch (SchedulerException se) {
            sched.getLog().error(
                    "Error notifying listeners of trigger misfire.", se);
            sched.notifySchedulerListenersError(
                    "Error notifying listeners of trigger misfire.", se);
        }
    }

    @Override
    public void notifySchedulerListenersFinalized(Trigger trigger) {
        sched.notifySchedulerListenersFinalized(trigger);
    }

    @Override
    public void signalSchedulingChange(long candidateNewNextFireTime) {
        schedThread.signalSchedulingChange(candidateNewNextFireTime);
    }

    @Override
    public void notifySchedulerListenersJobDeleted(JobKey jobKey) {
        sched.notifySchedulerListenersJobDeleted(jobKey);
    }

    @Override
    public void notifySchedulerListenersError(String string, SchedulerException jpe) {
        sched.notifySchedulerListenersError(string, jpe);
    }
}
