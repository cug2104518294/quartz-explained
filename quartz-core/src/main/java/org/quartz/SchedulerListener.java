package org.quartz;

/**
 * The interface to be implemented by classes that want to be informed of major
 * <code>{@link Scheduler}</code> events.
 * <p>
 * <p>
 * Scheduler的listener接口，建议继承{@link org.quartz.listeners.SchedulerListenerSupport}
 *
 * @author James House
 * @see Scheduler
 * @see JobListener
 * @see TriggerListener
 */
public interface SchedulerListener {


    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * is scheduled.
     * </p>
     */
    void jobScheduled(Trigger trigger);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * is unscheduled.
     * </p>
     *
     * @see SchedulerListener#schedulingDataCleared()
     */
    void jobUnscheduled(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has reached the condition in which it will never fire again.
     * </p>
     * <p>
     * trigger终止执行时调用
     */
    void triggerFinalized(Trigger trigger);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has been paused.
     * </p>
     */
    void triggerPaused(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a
     * group of <code>{@link Trigger}s</code> has been paused.
     * </p>
     *
     * <p>If all groups were paused then triggerGroup will be null</p>
     *
     * @param triggerGroup the paused group, or null if all were paused
     */
    void triggersPaused(String triggerGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has been un-paused.
     * </p>
     */
    void triggerResumed(TriggerKey triggerKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a
     * group of <code>{@link Trigger}s</code> has been un-paused.
     * </p>
     */
    void triggersResumed(String triggerGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been added.
     * </p>
     */
    void jobAdded(JobDetail jobDetail);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been deleted.
     * </p>
     */
    void jobDeleted(JobKey jobKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been paused.
     * </p>
     */
    void jobPaused(JobKey jobKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a
     * group of <code>{@link org.quartz.JobDetail}s</code> has been paused.
     * </p>
     *
     * @param jobGroup the paused group, or null if all were paused
     */
    void jobsPaused(String jobGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link org.quartz.JobDetail}</code>
     * has been un-paused.
     * </p>
     */
    void jobResumed(JobKey jobKey);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a
     * group of <code>{@link org.quartz.JobDetail}s</code> has been un-paused.
     * </p>
     */
    void jobsResumed(String jobGroup);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a serious error has
     * occurred within the scheduler - such as repeated failures in the <code>JobStore</code>,
     * or the inability to instantiate a <code>Job</code> instance when its
     * <code>Trigger</code> has fired.
     * </p>
     *
     * <p>
     * The <code>getErrorCode()</code> method of the given SchedulerException
     * can be used to determine more specific information about the type of
     * error that was encountered.
     * </p>
     */
    void schedulerError(String msg, SchedulerException cause);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has move to standby mode.
     * </p>
     */
    void schedulerInStandbyMode();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has started.
     * </p>
     */
    void schedulerStarted();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it is starting.
     * </p>
     */
    void schedulerStarting();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has shutdown.
     * </p>
     */
    void schedulerShutdown();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that it has begun the shutdown sequence.
     * </p>
     */
    void schedulerShuttingdown();

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener
     * that all jobs, triggers and calendars were deleted.
     */
    void schedulingDataCleared();
}
