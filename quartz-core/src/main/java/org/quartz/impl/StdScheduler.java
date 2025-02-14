package org.quartz.impl;

import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An implementation of the <code>Scheduler</code> interface that directly
 * proxies all method calls to the equivalent call on a given <code>QuartzScheduler</code>
 * instance.
 * </p>
 *
 * <p>
 * Scheduler的一种实现，使用代理模式，将所有的调用代理给QuartzScheduler。
 * </p>
 *
 * @author James House
 * @see org.quartz.Scheduler
 * @see org.quartz.core.QuartzScheduler
 */
public class StdScheduler implements Scheduler {

    private QuartzScheduler sched;

    /**
     * <p>
     * Construct a <code>StdScheduler</code> instance to proxy the given
     * <code>QuartzScheduler</code> instance, and with the given <code>SchedulingContext</code>.
     * </p>
     */
    public StdScheduler(QuartzScheduler sched) {
        this.sched = sched;
    }

    /**
     * <p>
     * Returns the name of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerName() {
        return sched.getSchedulerName();
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerInstanceId() {
        return sched.getSchedulerInstanceId();
    }

    @Override
    public SchedulerMetaData getMetaData() {
        return new SchedulerMetaData(getSchedulerName(),
                getSchedulerInstanceId(), getClass(), false, isStarted(),
                isInStandbyMode(), isShutdown(), sched.runningSince(),
                sched.numJobsExecuted(), sched.getJobStoreClass(),
                sched.supportsPersistence(), sched.isClustered(), sched.getThreadPoolClass(),
                sched.getThreadPoolSize(), sched.getVersion());

    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public SchedulerContext getContext() throws SchedulerException {
        return sched.getSchedulerContext();
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void start() throws SchedulerException {
        sched.start();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void startDelayed(int seconds) throws SchedulerException {
        sched.startDelayed(seconds);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void standby() {
        sched.standby();
    }

    /**
     * Whether the scheduler has been started.
     *
     * <p>
     * Note: This only reflects whether <code>{@link #start()}</code> has ever
     * been called on this Scheduler, so it will return <code>true</code> even
     * if the <code>Scheduler</code> is currently in standby mode or has been
     * since shutdown.
     * </p>
     *
     * @see #start()
     * @see #isShutdown()
     * @see #isInStandbyMode()
     */
    @Override
    public boolean isStarted() {
        return (sched.runningSince() != null);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isInStandbyMode() {
        return sched.isInStandbyMode();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown() {
        sched.shutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        sched.shutdown(waitForJobsToComplete);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isShutdown() {
        return sched.isShutdown();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<JobExecutionContext> getCurrentlyExecutingJobs() {
        return sched.getCurrentlyExecutingJobs();
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void clear() throws SchedulerException {
        sched.clear();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws SchedulerException {
        return sched.scheduleJob(jobDetail, trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        return sched.scheduleJob(trigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addJob(JobDetail jobDetail, boolean replace)
            throws SchedulerException {
        sched.addJob(jobDetail, replace);
    }

    @Override
    public void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling)
            throws SchedulerException {
        sched.addJob(jobDetail, replace, storeNonDurableWhileAwaitingScheduling);
    }


    @Override
    public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException {
        return sched.deleteJobs(jobKeys);
    }

    @Override
    public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException {
        sched.scheduleJobs(triggersAndJobs, replace);
    }

    @Override
    public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException {
        sched.scheduleJob(jobDetail, triggersForJob, replace);
    }

    @Override
    public boolean unscheduleJobs(List<TriggerKey> triggerKeys)
            throws SchedulerException {
        return sched.unscheduleJobs(triggerKeys);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteJob(JobKey jobKey)
            throws SchedulerException {
        return sched.deleteJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean unscheduleJob(TriggerKey triggerKey)
            throws SchedulerException {
        return sched.unscheduleJob(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date rescheduleJob(TriggerKey triggerKey,
                              Trigger newTrigger) throws SchedulerException {
        return sched.rescheduleJob(triggerKey, newTrigger);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void triggerJob(JobKey jobKey)
            throws SchedulerException {
        triggerJob(jobKey, null);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void triggerJob(JobKey jobKey, JobDataMap data)
            throws SchedulerException {
        sched.triggerJob(jobKey, data);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        sched.pauseTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        sched.pauseTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJob(JobKey jobKey)
            throws SchedulerException {
        sched.pauseJob(jobKey);
    }

    /**
     * @see org.quartz.Scheduler#getPausedTriggerGroups()
     */
    @Override
    public Set<String> getPausedTriggerGroups() throws SchedulerException {
        return sched.getPausedTriggerGroups();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        sched.pauseJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        sched.resumeTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        sched.resumeTriggers(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJob(JobKey jobKey)
            throws SchedulerException {
        sched.resumeJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        sched.resumeJobs(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseAll() throws SchedulerException {
        sched.pauseAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeAll() throws SchedulerException {
        sched.resumeAll();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getJobGroupNames() throws SchedulerException {
        return sched.getJobGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<? extends Trigger> getTriggersOfJob(JobKey jobKey)
            throws SchedulerException {
        return sched.getTriggersOfJob(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
        return sched.getJobKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getTriggerGroupNames() throws SchedulerException {
        return sched.getTriggerGroupNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        return sched.getTriggerKeys(matcher);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public JobDetail getJobDetail(JobKey jobKey)
            throws SchedulerException {
        return sched.getJobDetail(jobKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Trigger getTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        return sched.getTrigger(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public TriggerState getTriggerState(TriggerKey triggerKey)
            throws SchedulerException {
        return sched.getTriggerState(triggerKey);
    }

    /**
     * Reset the current state of the identified <code>{@link Trigger}</code>
     * from {@link TriggerState#ERROR} to {@link TriggerState#NORMAL} or
     * {@link TriggerState#PAUSED} as appropriate.
     *
     * <p>Only affects triggers that are in ERROR state - if identified trigger is not
     * in that state then the result is a no-op.</p>
     *
     * <p>The result will be the trigger returning to the normal, waiting to
     * be fired state, unless the trigger's group has been paused, in which
     * case it will go into the PAUSED state.</p>
     *
     * @see Trigger.TriggerState
     */
    @Override
    public void resetTriggerFromErrorState(TriggerKey triggerKey)
            throws SchedulerException {
        sched.resetTriggerFromErrorState(triggerKey);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
            throws SchedulerException {
        sched.addCalendar(calName, calendar, replace, updateTriggers);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteCalendar(String calName) throws SchedulerException {
        return sched.deleteCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Calendar getCalendar(String calName) throws SchedulerException {
        return sched.getCalendar(calName);
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getCalendarNames() throws SchedulerException {
        return sched.getCalendarNames();
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(JobKey jobKey) throws SchedulerException {
        return sched.checkExists(jobKey);
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(TriggerKey triggerKey) throws SchedulerException {
        return sched.checkExists(triggerKey);
    }


    /**
     * @see org.quartz.Scheduler#setJobFactory(org.quartz.spi.JobFactory)
     */
    @Override
    public void setJobFactory(JobFactory factory) throws SchedulerException {
        sched.setJobFactory(factory);
    }

    /**
     * @see org.quartz.Scheduler#getListenerManager()
     */
    @Override
    public ListenerManager getListenerManager() throws SchedulerException {
        return sched.getListenerManager();
    }

    @Override
    public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
        return sched.interrupt(jobKey);
    }

    @Override
    public boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException {
        return sched.interrupt(fireInstanceId);
    }


}
