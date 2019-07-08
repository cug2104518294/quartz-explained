package org.quartz.spi;

import org.quartz.Calendar;
import org.quartz.JobDetail;

import java.util.Date;

/**
 * <p>
 * A simple class (structure) used for returning execution-time data from the
 * JobStore to the <code>QuartzSchedulerThread</code>.
 * </p>
 *
 * @author James House
 * @see org.quartz.core.QuartzSchedulerThread
 */
public class TriggerFiredBundle implements java.io.Serializable {

    private static final long serialVersionUID = -6414106108306999265L;

    private JobDetail job;

    private OperableTrigger trigger;

    private Calendar cal;

    private boolean jobIsRecovering;

    private Date fireTime;

    private Date scheduledFireTime;

    private Date prevFireTime;

    private Date nextFireTime;

    public TriggerFiredBundle(JobDetail job, OperableTrigger trigger, Calendar cal,
                              boolean jobIsRecovering, Date fireTime, Date scheduledFireTime,
                              Date prevFireTime, Date nextFireTime) {
        this.job = job;
        this.trigger = trigger;
        this.cal = cal;
        this.jobIsRecovering = jobIsRecovering;
        this.fireTime = fireTime;
        this.scheduledFireTime = scheduledFireTime;
        this.prevFireTime = prevFireTime;
        this.nextFireTime = nextFireTime;
    }

    public JobDetail getJobDetail() {
        return job;
    }

    public OperableTrigger getTrigger() {
        return trigger;
    }

    public Calendar getCalendar() {
        return cal;
    }

    public boolean isRecovering() {
        return jobIsRecovering;
    }

    /**
     * @return Returns the fireTime.
     */
    public Date getFireTime() {
        return fireTime;
    }

    /**
     * @return Returns the nextFireTime.
     */
    public Date getNextFireTime() {
        return nextFireTime;
    }

    /**
     * @return Returns the prevFireTime.
     */
    public Date getPrevFireTime() {
        return prevFireTime;
    }

    /**
     * @return Returns the scheduledFireTime.
     */
    public Date getScheduledFireTime() {
        return scheduledFireTime;
    }

}