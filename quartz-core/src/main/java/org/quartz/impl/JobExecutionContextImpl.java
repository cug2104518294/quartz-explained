package org.quartz.impl;

import org.quartz.*;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

import java.util.Date;
import java.util.HashMap;


public class JobExecutionContextImpl implements java.io.Serializable, JobExecutionContext {

    private static final long serialVersionUID = -8139417614523942021L;

    private transient Scheduler scheduler;

    private Trigger trigger;

    private JobDetail jobDetail;

    private JobDataMap jobDataMap;

    private transient Job job;

    private Calendar calendar;

    private boolean recovering = false;

    private int numRefires = 0;

    private Date fireTime;

    private Date scheduledFireTime;

    private Date prevFireTime;

    private Date nextFireTime;

    private long jobRunTime = -1;

    private Object result;

    private HashMap<Object, Object> data = new HashMap<Object, Object>();

    /**
     * <p>
     * Create a JobExcecutionContext with the given context data.
     * </p>
     */
    public JobExecutionContextImpl(Scheduler scheduler,
                                   TriggerFiredBundle firedBundle, Job job) {
        this.scheduler = scheduler;
        this.trigger = firedBundle.getTrigger();
        this.calendar = firedBundle.getCalendar();
        this.jobDetail = firedBundle.getJobDetail();
        this.job = job;
        this.recovering = firedBundle.isRecovering();
        this.fireTime = firedBundle.getFireTime();
        this.scheduledFireTime = firedBundle.getScheduledFireTime();
        this.prevFireTime = firedBundle.getPrevFireTime();
        this.nextFireTime = firedBundle.getNextFireTime();

        this.jobDataMap = new JobDataMap();
        this.jobDataMap.putAll(jobDetail.getJobDataMap());
        this.jobDataMap.putAll(trigger.getJobDataMap());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecovering() {
        return recovering;
    }

    @Override
    public TriggerKey getRecoveringTriggerKey() {
        if (isRecovering()) {
            return new TriggerKey(jobDataMap.getString(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_NAME),
                    jobDataMap.getString(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_GROUP));
        } else {
            throw new IllegalStateException("Not a recovering job");
        }
    }

    public void incrementRefireCount() {
        numRefires++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRefireCount() {
        return numRefires;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobDataMap getMergedJobDataMap() {
        return jobDataMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobDetail getJobDetail() {
        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job getJobInstance() {
        return job;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getFireTime() {
        return fireTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getScheduledFireTime() {
        return scheduledFireTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getPreviousFireTime() {
        return prevFireTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    @Override
    public String toString() {
        return "JobExecutionContext:" + " trigger: '"
                + getTrigger().getKey() + " job: "
                + getJobDetail().getKey() + " fireTime: '" + getFireTime()
                + " scheduledFireTime: " + getScheduledFireTime()
                + " previousFireTime: '" + getPreviousFireTime()
                + " nextFireTime: " + getNextFireTime() + " isRecovering: "
                + isRecovering() + " refireCount: " + getRefireCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult() {
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getJobRunTime() {
        return jobRunTime;
    }

    /**
     * @param jobRunTime The jobRunTime to set.
     */
    public void setJobRunTime(long jobRunTime) {
        this.jobRunTime = jobRunTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(Object key, Object value) {
        data.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object key) {
        return data.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFireInstanceId() {
        return ((OperableTrigger) trigger).getFireInstanceId();
    }
}
