package org.quartz.simpl;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This is class is a simple implementation of a zero size thread pool, based on the
 * <code>{@link org.quartz.spi.ThreadPool}</code> interface.
 * </p>
 *
 * <p>
 * The pool has zero <code>Thread</code>s and does not grow or shrink based on demand.
 * Which means it is obviously not useful for most scenarios.  When it may be useful
 * is to prevent creating any worker threads at all - which may be desirable for
 * the sole purpose of preserving system resources in the case where the scheduler
 * instance only exists in order to schedule jobs, but which will never execute
 * jobs (e.g. will never have start() called on it).
 * </p>
 * <p>
 * <p>
 * 线程池的大小为0，不会伸缩，所以在绝大部分场景下该线程池都是用不到的。
 * <p>
 * 唯一可能用到的场景：scheduler只是用于调度job，而不执行job。
 *
 * @author Wayne Fay
 */
public class ZeroSizeThreadPool implements ThreadPool {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * Create a new <code>ZeroSizeThreadPool</code>.
     * </p>
     */
    public ZeroSizeThreadPool() {
    }

    public Logger getLog() {
        return log;
    }

    @Override
    public int getPoolSize() {
        return 0;
    }

    @Override
    public void initialize() throws SchedulerConfigException {
    }

    public void shutdown() {
        shutdown(true);
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        getLog().debug("shutdown complete");
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        throw new UnsupportedOperationException("This ThreadPool should not be used on Scheduler instances that are start()ed.");
    }

    @Override
    public int blockForAvailableThreads() {
        throw new UnsupportedOperationException("This ThreadPool should not be used on Scheduler instances that are start()ed.");
    }

    @Override
    public void setInstanceId(String schedInstId) {
    }

    @Override
    public void setInstanceName(String schedName) {
    }
}
