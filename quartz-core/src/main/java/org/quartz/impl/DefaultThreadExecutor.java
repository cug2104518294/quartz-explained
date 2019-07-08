package org.quartz.impl;

import org.quartz.spi.ThreadExecutor;

/**
 * Schedules work on a newly spawned thread. This is the default Quartz
 * behavior.
 * <p>
 * 默认在新的线程执行调度任务
 */
public class DefaultThreadExecutor implements ThreadExecutor {

    @Override
    public void initialize() {
    }

    @Override
    public void execute(Thread thread) {
        thread.start();
    }

}
