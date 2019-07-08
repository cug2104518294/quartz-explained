package org.quartz;

import java.io.Serializable;

/**
 * Conveys the detail properties of a given <code>Job</code> instance. JobDetails are
 * to be created/defined with {@link JobBuilder}.
 *
 * <p>
 * Quartz does not store an actual instance of a <code>Job</code> class, but
 * instead allows you to define an instance of one, through the use of a <code>JobDetail</code>.
 * </p>
 *
 * <p>
 * <code>Job</code>s have a name and group associated with them, which
 * should uniquely identify them within a single <code>{@link Scheduler}</code>.
 * </p>
 *
 * <p>
 * <code>Trigger</code>s are the 'mechanism' by which <code>Job</code>s
 * are scheduled. Many <code>Trigger</code>s can point to the same <code>Job</code>,
 * but a single <code>Trigger</code> can only point to one <code>Job</code>.
 * </p>
 * <p>
 * <p>
 * JobDetail可以看作Job的属性配置，通过JobBuilder创建。
 *
 * @author James House
 * @see JobBuilder
 * @see Job
 * @see JobDataMap
 * @see Trigger
 */
public interface JobDetail extends Serializable, Cloneable {

    JobKey getKey();

    /**
     * <p>
     * Return the description given to the <code>Job</code> instance by its
     * creator (if any).
     * </p>
     *
     * @return null if no description was set.
     */
    String getDescription();

    /**
     * <p>
     * Get the instance of <code>Job</code> that will be executed.
     * </p>
     */
    Class<? extends Job> getJobClass();

    /**
     * <p>
     * Get the <code>JobDataMap</code> that is associated with the <code>Job</code>.
     * </p>
     */
    JobDataMap getJobDataMap();

    /**
     * <p>
     * Whether or not the <code>Job</code> should remain stored after it is
     * orphaned (no <code>{@link Trigger}s</code> point to it).
     * </p>
     *
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if the Job should remain persisted after
     * being orphaned.
     */
    boolean isDurable();

    /**
     * @return whether the associated Job class carries the {@link PersistJobDataAfterExecution} annotation.
     * @see PersistJobDataAfterExecution
     */
    boolean isPersistJobDataAfterExecution();

    /**
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     * @see DisallowConcurrentExecution
     */
    boolean isConcurrentExectionDisallowed();

    /**
     * <p>
     * Instructs the <code>Scheduler</code> whether or not the <code>Job</code>
     * should be re-executed if a 'recovery' or 'fail-over' situation is
     * encountered.
     * </p>
     *
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     *
     * @see JobExecutionContext#isRecovering()
     */
    boolean requestsRecovery();

    Object clone();

    /**
     * Get a {@link JobBuilder} that is configured to produce a
     * <code>JobDetail</code> identical to this one.
     */
    JobBuilder getJobBuilder();

}