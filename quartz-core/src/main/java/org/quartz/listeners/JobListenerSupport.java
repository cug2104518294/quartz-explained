package org.quartz.listeners;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helpful abstract base class for implementors of
 * <code>{@link org.quartz.JobListener}</code>.
 *
 * <p>
 * The methods in this class are empty so you only need to override the
 * subset for the <code>{@link org.quartz.JobListener}</code> events
 * you care about.
 * </p>
 *
 * <p>
 * You are required to implement <code>{@link org.quartz.JobListener#getName()}</code>
 * to return the unique name of your <code>JobListener</code>.
 * </p>
 *
 * @see org.quartz.JobListener
 */
public abstract class JobListenerSupport implements JobListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get the <code>{@link org.slf4j.Logger}</code> for this
     * class's category.  This should be used by subclasses for logging.
     */
    protected Logger getLog() {
        return log;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    }
}
