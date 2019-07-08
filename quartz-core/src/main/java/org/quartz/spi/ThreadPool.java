package org.quartz.spi;

import org.quartz.SchedulerConfigException;

/**
 * <p>
 * The interface to be implemented by classes that want to provide a thread
 * pool for the <code>{@link org.quartz.core.QuartzScheduler}</code>'s use.
 * </p>
 *
 * <p>
 * <code>ThreadPool</code> implementation instances should ideally be made
 * for the sole use of Quartz.  Most importantly, when the method
 * <code>blockForAvailableThreads()</code> returns a value of 1 or greater,
 * there must still be at least one available thread in the pool when the
 * method <code>runInThread(Runnable)</code> is called a few moments (or
 * many moments) later.  If this assumption does not hold true, it may
 * result in extra JobStore queries and updates, and if clustering features
 * are being used, it may result in greater imballance of load.
 * </p>
 *
 * <p>
 * 线程池接口，仅限于QuartzScheduler使用
 * </p>
 *
 * @author James House
 * @see org.quartz.core.QuartzScheduler
 */
public interface ThreadPool {

    /**
     * <p>
     * Execute the given <code>{@link java.lang.Runnable}</code> in the next
     * available <code>Thread</code>.
     * </p>
     *
     * <p>
     * The implementation of this interface should not throw exceptions unless
     * there is a serious problem (i.e. a serious misconfiguration). If there
     * are no immediately available threads <code>false</code> should be returned.
     * </p>
     *
     * <p>
     * 在空闲线程上执行，如果当前没有可用的空闲线程，返回false。
     * 该实现不应该抛出异常，除非遇到严重问题（如严重的配置错误）
     * </p>
     *
     * @return true, if the runnable was assigned to run on a Thread.
     */
    boolean runInThread(Runnable runnable);

    /**
     * <p>
     * Determines the number of threads that are currently available in in
     * the pool.  Useful for determining the number of times
     * <code>runInThread(Runnable)</code> can be called before returning
     * false.
     * </p>
     *
     * <p>The implementation of this method should block until there is at
     * least one available thread.</p>
     * <p>
     * 返回线程池中当前可用线程的数量，也是`runInThread(Runnable)`可以被调用的次数
     * <p>
     * 如果线程池中没有可用线程，该方法会block
     *
     * @return the number of currently available threads
     */
    int blockForAvailableThreads();

    /**
     * <p>
     * Must be called before the <code>ThreadPool</code> is
     * used, in order to give the it a chance to initialize.
     * </p>
     *
     * <p>Typically called by the <code>SchedulerFactory</code>.</p>
     */
    void initialize() throws SchedulerConfigException;

    /**
     * <p>
     * Called by the QuartzScheduler to inform the <code>ThreadPool</code>
     * that it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     */
    void shutdown(boolean waitForJobsToComplete);

    /**
     * Get the current number of threads in the <code>ThreadPool</code>.
     */
    int getPoolSize();

    /**
     * Inform the <code>ThreadPool</code> of the Scheduler instance's Id,
     * prior to initialize being invoked.
     */
    void setInstanceId(String schedInstId);

    /**
     * Inform the <code>ThreadPool</code> of the Scheduler instance's name,
     * prior to initialize being invoked.
     */
    void setInstanceName(String schedName);

}
