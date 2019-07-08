package org.quartz.listeners;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helpful abstract base class for implementors of
 * <code>{@link org.quartz.SchedulerListener}</code>.
 *
 * <p>
 * The methods in this class are empty so you only need to override the
 * subset for the <code>{@link org.quartz.SchedulerListener}</code> events
 * you care about.
 * </p>
 *
 * @see org.quartz.SchedulerListener
 */
public abstract class SchedulerListenerSupport implements SchedulerListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get the <code>{@link org.slf4j.Logger}</code> for this
     * class's category.  This should be used by subclasses for logging.
     */
    protected Logger getLog() {
        return log;
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
    }

    @Override
    public void jobPaused(JobKey jobKey) {
    }

    @Override
    public void jobResumed(JobKey jobKey) {
    }

    @Override
    public void jobScheduled(Trigger trigger) {
    }

    @Override
    public void jobsPaused(String jobGroup) {
    }

    @Override
    public void jobsResumed(String jobGroup) {
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
    }

    @Override
    public void schedulerInStandbyMode() {
    }

    @Override
    public void schedulerShutdown() {
    }

    @Override
    public void schedulerShuttingdown() {
    }

    @Override
    public void schedulerStarted() {
    }

    @Override
    public void schedulerStarting() {
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
    }

    @Override
    public void triggersPaused(String triggerGroup) {
    }

    @Override
    public void triggersResumed(String triggerGroup) {
    }

    @Override
    public void schedulingDataCleared() {
    }

}
