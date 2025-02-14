package org.quartz;

/**
 * An exception that can be thrown by a <code>{@link org.quartz.Job}</code>
 * to indicate to the Quartz <code>{@link Scheduler}</code> that an error
 * occurred while executing, and whether or not the <code>Job</code> requests
 * to be re-fired immediately (using the same <code>{@link JobExecutionContext}</code>,
 * or whether it wants to be unscheduled.
 *
 * <p>
 * Note that if the flag for 'refire immediately' is set, the flags for
 * unscheduling the Job are ignored.
 * </p>
 *
 * @author James House
 * @see Job
 * @see JobExecutionContext
 * @see SchedulerException
 */
public class JobExecutionException extends SchedulerException {

    private static final long serialVersionUID = 1326342535829043325L;

    private boolean refire = false;

    private boolean unscheduleTrigg = false;

    private boolean unscheduleAllTriggs = false;

    /**
     * <p>
     * Create a JobExcecutionException, with the 're-fire immediately' flag set
     * to <code>false</code>.
     * </p>
     */
    public JobExecutionException() {
    }

    /**
     * <p>
     * Create a JobExcecutionException, with the given cause.
     * </p>
     */
    public JobExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>
     * Create a JobExcecutionException, with the given message.
     * </p>
     */
    public JobExecutionException(String msg) {
        super(msg);
    }

    /**
     * <p>
     * Create a JobExcecutionException with the 're-fire immediately' flag set
     * to the given value.
     * </p>
     */
    public JobExecutionException(boolean refireImmediately) {
        refire = refireImmediately;
    }

    /**
     * <p>
     * Create a JobExcecutionException with the given underlying exception, and
     * the 're-fire immediately' flag set to the given value.
     * </p>
     */
    public JobExecutionException(Throwable cause, boolean refireImmediately) {
        super(cause);
        refire = refireImmediately;
    }

    /**
     * <p>
     * Create a JobExcecutionException with the given message, and underlying
     * exception.
     * </p>
     */
    public JobExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * <p>
     * Create a JobExcecutionException with the given message, and underlying
     * exception, and the 're-fire immediately' flag set to the given value.
     * </p>
     */
    public JobExecutionException(String msg, Throwable cause,
                                 boolean refireImmediately) {
        super(msg, cause);
        refire = refireImmediately;
    }

    /**
     * Create a JobExcecutionException with the given message and the 're-fire
     * immediately' flag set to the given value.
     */
    public JobExecutionException(String msg, boolean refireImmediately) {
        super(msg);

        refire = refireImmediately;
    }

    public void setRefireImmediately(boolean refire) {
        this.refire = refire;
    }

    public boolean refireImmediately() {
        return refire;
    }

    public void setUnscheduleFiringTrigger(boolean unscheduleTrigg) {
        this.unscheduleTrigg = unscheduleTrigg;
    }

    public boolean unscheduleFiringTrigger() {
        return unscheduleTrigg;
    }

    public void setUnscheduleAllTriggers(boolean unscheduleAllTriggs) {
        this.unscheduleAllTriggs = unscheduleAllTriggs;
    }

    public boolean unscheduleAllTriggers() {
        return unscheduleAllTriggs;
    }

}
