package org.quartz;

/**
 * <p>
 * The interface to be implemented by classes which represent a 'job' to be
 * performed.
 * </p>
 *
 * <p>
 * Instances of <code>Job</code> must have a <code>public</code>
 * no-argument constructor.
 * </p>
 *
 * <p>
 * <code>JobDataMap</code> provides a mechanism for 'instance member data'
 * that may be required by some implementations of this interface.
 * </p>
 *
 * @author James House
 * @see JobDetail
 * @see JobBuilder
 * @see ExecuteInJTATransaction
 * @see DisallowConcurrentExecution
 * @see PersistJobDataAfterExecution
 * @see Trigger
 * @see Scheduler
 */
public interface Job {

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     *
     * <p>
     * The implementation may wish to set a
     * {@link JobExecutionContext#setResult(Object) result} object on the
     * {@link JobExecutionContext} before this method exits.  The result itself
     * is meaningless to Quartz, but may be informative to
     * <code>{@link JobListener}s</code> or
     * <code>{@link TriggerListener}s</code> that are watching the job's
     * execution.
     * </p>
     *
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    void execute(JobExecutionContext context)
            throws JobExecutionException;

}
