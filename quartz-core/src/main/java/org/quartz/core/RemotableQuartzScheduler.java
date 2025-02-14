package org.quartz.core;

import org.quartz.*;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.OperableTrigger;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RemotableQuartzScheduler extends Remote {

    String getSchedulerName() throws RemoteException;

    String getSchedulerInstanceId() throws RemoteException;

    SchedulerContext getSchedulerContext() throws SchedulerException, RemoteException;

    void start() throws SchedulerException, RemoteException;

    void startDelayed(int seconds) throws SchedulerException, RemoteException;

    void standby() throws RemoteException;

    boolean isInStandbyMode() throws RemoteException;

    void shutdown() throws RemoteException;

    void shutdown(boolean waitForJobsToComplete) throws RemoteException;

    boolean isShutdown() throws RemoteException;

    Date runningSince() throws RemoteException;

    String getVersion() throws RemoteException;

    int numJobsExecuted() throws RemoteException;

    Class<?> getJobStoreClass() throws RemoteException;

    boolean supportsPersistence() throws RemoteException;

    boolean isClustered() throws RemoteException;

    Class<?> getThreadPoolClass() throws RemoteException;

    int getThreadPoolSize() throws RemoteException;

    void clear() throws SchedulerException, RemoteException;

    List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException, RemoteException;

    Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException, RemoteException;

    Date scheduleJob(Trigger trigger) throws SchedulerException, RemoteException;

    void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException, RemoteException;

    void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling) throws SchedulerException, RemoteException;

    boolean deleteJob(JobKey jobKey) throws SchedulerException, RemoteException;

    boolean unscheduleJob(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) throws SchedulerException, RemoteException;

    void triggerJob(JobKey jobKey, JobDataMap data) throws SchedulerException, RemoteException;

    void triggerJob(OperableTrigger trig) throws SchedulerException, RemoteException;

    void pauseTrigger(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException, RemoteException;

    void pauseJob(JobKey jobKey) throws SchedulerException, RemoteException;

    void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException, RemoteException;

    void resumeTrigger(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException, RemoteException;

    Set<String> getPausedTriggerGroups() throws SchedulerException, RemoteException;

    void resumeJob(JobKey jobKey) throws SchedulerException, RemoteException;

    void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException, RemoteException;

    void pauseAll() throws SchedulerException, RemoteException;

    void resumeAll() throws SchedulerException, RemoteException;

    List<String> getJobGroupNames() throws SchedulerException, RemoteException;

    Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException, RemoteException;

    List<? extends Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException, RemoteException;

    List<String> getTriggerGroupNames() throws SchedulerException, RemoteException;

    Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException, RemoteException;

    JobDetail getJobDetail(JobKey jobKey) throws SchedulerException, RemoteException;

    Trigger getTrigger(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    TriggerState getTriggerState(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    void resetTriggerFromErrorState(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException, RemoteException;

    boolean deleteCalendar(String calName) throws SchedulerException, RemoteException;

    Calendar getCalendar(String calName) throws SchedulerException, RemoteException;

    List<String> getCalendarNames() throws SchedulerException, RemoteException;

    boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException, RemoteException;

    boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException, RemoteException;

    boolean checkExists(JobKey jobKey) throws SchedulerException, RemoteException;

    boolean checkExists(TriggerKey triggerKey) throws SchedulerException, RemoteException;

    public boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException, RemoteException;

    public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException, RemoteException;

    public void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException, RemoteException;

    public boolean unscheduleJobs(List<TriggerKey> triggerKeys) throws SchedulerException, RemoteException;

}
