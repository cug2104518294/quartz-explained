package org.quartz.impl;

import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.core.RemotableQuartzScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * An implementation of the <code>Scheduler</code> interface that remotely
 * proxies all method calls to the equivalent call on a given <code>QuartzScheduler</code>
 * instance, via RMI.
 * </p>
 *
 * @author James House
 * @see org.quartz.Scheduler
 * @see org.quartz.core.QuartzScheduler
 */
public class RemoteScheduler implements Scheduler {

    private RemotableQuartzScheduler rsched;

    private String schedId;

    private String rmiHost;

    private int rmiPort;

    /**
     * <p>
     * Construct a <code>RemoteScheduler</code> instance to proxy the given
     * <code>RemoteableQuartzScheduler</code> instance, and with the given
     * <code>SchedulingContext</code>.
     * </p>
     */
    public RemoteScheduler(String schedId, String host, int port) {
        this.schedId = schedId;
        this.rmiHost = host;
        this.rmiPort = port;
    }

    protected RemotableQuartzScheduler getRemoteScheduler()
            throws SchedulerException {
        if (rsched != null) {
            return rsched;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(rmiHost, rmiPort);
            rsched = (RemotableQuartzScheduler) registry.lookup(schedId);
        } catch (Exception e) {
            SchedulerException initException = new SchedulerException(
                    "Could not get handle to remote scheduler: "
                            + e.getMessage(), e);
            throw initException;
        }
        return rsched;
    }

    protected SchedulerException invalidateHandleCreateException(String msg, Exception cause) {
        rsched = null;
        SchedulerException ex = new SchedulerException(msg, cause);
        return ex;
    }

    /**
     * <p>
     * Returns the name of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerName() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerName();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Returns the instance Id of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public String getSchedulerInstanceId() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerInstanceId();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public SchedulerMetaData getMetaData() throws SchedulerException {
        try {
            RemotableQuartzScheduler sched = getRemoteScheduler();
            return new SchedulerMetaData(getSchedulerName(),
                    getSchedulerInstanceId(), getClass(), true, isStarted(),
                    isInStandbyMode(), isShutdown(), sched.runningSince(),
                    sched.numJobsExecuted(), sched.getJobStoreClass(),
                    sched.supportsPersistence(), sched.isClustered(), sched.getThreadPoolClass(),
                    sched.getThreadPoolSize(), sched.getVersion());

        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }

    }

    /**
     * <p>
     * Returns the <code>SchedulerContext</code> of the <code>Scheduler</code>.
     * </p>
     */
    @Override
    public SchedulerContext getContext() throws SchedulerException {
        try {
            return getRemoteScheduler().getSchedulerContext();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void start() throws SchedulerException {
        try {
            getRemoteScheduler().start();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void startDelayed(int seconds) throws SchedulerException {
        try {
            getRemoteScheduler().startDelayed(seconds);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void standby() throws SchedulerException {
        try {
            getRemoteScheduler().standby();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
    public boolean isStarted() throws SchedulerException {
        try {
            return (getRemoteScheduler().runningSince() != null);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isInStandbyMode() throws SchedulerException {
        try {
            return getRemoteScheduler().isInStandbyMode();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown() throws SchedulerException {
        try {
            String schedulerName = getSchedulerName();

            getRemoteScheduler().shutdown();

            SchedulerRepository.getInstance().remove(schedulerName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void shutdown(boolean waitForJobsToComplete)
            throws SchedulerException {
        try {
            String schedulerName = getSchedulerName();

            getRemoteScheduler().shutdown(waitForJobsToComplete);

            SchedulerRepository.getInstance().remove(schedulerName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean isShutdown() throws SchedulerException {
        try {
            return getRemoteScheduler().isShutdown();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException {
        try {
            return getRemoteScheduler().getCurrentlyExecutingJobs();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Scheduling-related Methods
    ///
    ///////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger)
            throws SchedulerException {
        try {
            return getRemoteScheduler().scheduleJob(jobDetail,
                    trigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date scheduleJob(Trigger trigger) throws SchedulerException {
        try {
            return getRemoteScheduler().scheduleJob(trigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void addJob(JobDetail jobDetail, boolean replace)
            throws SchedulerException {
        try {
            getRemoteScheduler().addJob(jobDetail, replace);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling)
            throws SchedulerException {
        try {
            getRemoteScheduler().addJob(jobDetail, replace, storeNonDurableWhileAwaitingScheduling);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException {
        try {
            return getRemoteScheduler().deleteJobs(jobKeys);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException {
        try {
            getRemoteScheduler().scheduleJobs(triggersAndJobs, replace);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException {
        try {
            getRemoteScheduler().scheduleJob(jobDetail, triggersForJob, replace);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    @Override
    public boolean unscheduleJobs(List<TriggerKey> triggerKeys)
            throws SchedulerException {
        try {
            return getRemoteScheduler().unscheduleJobs(triggerKeys);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean deleteJob(JobKey jobKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler()
                    .deleteJob(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean unscheduleJob(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler().unscheduleJob(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Date rescheduleJob(TriggerKey triggerKey,
                              Trigger newTrigger) throws SchedulerException {
        try {
            return getRemoteScheduler().rescheduleJob(triggerKey,
                    newTrigger);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
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
        try {
            getRemoteScheduler().triggerJob(jobKey, data);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            getRemoteScheduler()
                    .pauseTrigger(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        try {
            getRemoteScheduler().pauseTriggers(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJob(JobKey jobKey)
            throws SchedulerException {
        try {
            getRemoteScheduler().pauseJob(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        try {
            getRemoteScheduler().pauseJobs(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            getRemoteScheduler().resumeTrigger(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        try {
            getRemoteScheduler().resumeTriggers(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJob(JobKey jobKey)
            throws SchedulerException {
        try {
            getRemoteScheduler().resumeJob(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException {
        try {
            getRemoteScheduler().resumeJobs(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void pauseAll() throws SchedulerException {
        try {
            getRemoteScheduler().pauseAll();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void resumeAll() throws SchedulerException {
        try {
            getRemoteScheduler().resumeAll();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getJobGroupNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getJobGroupNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException {
        try {
            return getRemoteScheduler().getJobKeys(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<? extends Trigger> getTriggersOfJob(JobKey jobKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggersOfJob(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public List<String> getTriggerGroupNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerGroupNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerKeys(matcher);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public JobDetail getJobDetail(JobKey jobKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler().getJobDetail(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean checkExists(JobKey jobKey) throws SchedulerException {
        try {
            return getRemoteScheduler().checkExists(jobKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public boolean checkExists(TriggerKey triggerKey) throws SchedulerException {
        try {
            return getRemoteScheduler().checkExists(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    @Override
    public void clear() throws SchedulerException {
        try {
            getRemoteScheduler().clear();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public Trigger getTrigger(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler().getTrigger(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public TriggerState getTriggerState(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            return getRemoteScheduler().getTriggerState(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void resetTriggerFromErrorState(TriggerKey triggerKey)
            throws SchedulerException {
        try {
            getRemoteScheduler().resetTriggerFromErrorState(triggerKey);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }


    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers)
            throws SchedulerException {
        try {
            getRemoteScheduler().addCalendar(calName, calendar,
                    replace, updateTriggers);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public boolean deleteCalendar(String calName) throws SchedulerException {
        try {
            return getRemoteScheduler().deleteCalendar(calName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public Calendar getCalendar(String calName) throws SchedulerException {
        try {
            return getRemoteScheduler().getCalendar(calName);
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * <p>
     * Calls the equivalent method on the 'proxied' <code>QuartzScheduler</code>.
     * </p>
     */
    public List<String> getCalendarNames() throws SchedulerException {
        try {
            return getRemoteScheduler().getCalendarNames();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }

    /**
     * @see org.quartz.Scheduler#getPausedTriggerGroups()
     */
    public Set<String> getPausedTriggerGroups() throws SchedulerException {
        try {
            return getRemoteScheduler().getPausedTriggerGroups();
        } catch (RemoteException re) {
            throw invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    ///
    /// Other Methods
    ///
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public ListenerManager getListenerManager() throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

    /**
     * @see org.quartz.Scheduler#interrupt(JobKey)
     */
    @Override
    public boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException {
        try {
            return getRemoteScheduler().interrupt(jobKey);
        } catch (RemoteException re) {
            throw new UnableToInterruptJobException(invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re));
        } catch (SchedulerException se) {
            throw new UnableToInterruptJobException(se);
        }
    }

    @Override
    public boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException {
        try {
            return getRemoteScheduler().interrupt(fireInstanceId);
        } catch (RemoteException re) {
            throw new UnableToInterruptJobException(invalidateHandleCreateException(
                    "Error communicating with remote scheduler.", re));
        } catch (SchedulerException se) {
            throw new UnableToInterruptJobException(se);
        }
    }

    /**
     * @see org.quartz.Scheduler#setJobFactory(org.quartz.spi.JobFactory)
     */
    @Override
    public void setJobFactory(JobFactory factory) throws SchedulerException {
        throw new SchedulerException(
                "Operation not supported for remote schedulers.");
    }

}
