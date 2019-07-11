package org.quartz.core;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * The thread responsible for performing the work of firing <code>{@link Trigger}</code>
 * s that are registered with the <code>{@link QuartzScheduler}</code>.
 * </p>
 * <p>
 * 负责触发Trigger执行的线程
 * <p>
 * 该线程类的主要工作分为以下几个步骤：
 * <p>
 * 等待QuartzScheduler启动
 * 查询待触发的Trigger
 * 等待Trigger触发时间到来
 * 触发Trigger
 * 循环上述步骤
 *
 * @author James House
 * @see QuartzScheduler
 * @see org.quartz.Job
 * @see Trigger
 */
public class QuartzSchedulerThread extends Thread {

    private QuartzScheduler qs;

    private QuartzSchedulerResources qsRsrcs;

    private final Object sigLock = new Object();

    private boolean signaled;
    private long signaledNextFireTime;

    private boolean paused;

    private AtomicBoolean halted;

    private Random random = new Random(System.currentTimeMillis());

    // When the scheduler finds there is no current trigger to fire, how long it should wait until checking again...
    private static long DEFAULT_IDLE_WAIT_TIME = 30L * 1000L;

    private long idleWaitTime = DEFAULT_IDLE_WAIT_TIME;

    private int idleWaitVariablness = 7 * 1000;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * <p>
     * Construct a new <code>QuartzSchedulerThread</code> for the given
     * <code>QuartzScheduler</code> as a non-daemon <code>Thread</code>
     * with normal priority.
     * </p>
     */
    QuartzSchedulerThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs) {
        this(qs, qsRsrcs, qsRsrcs.getMakeSchedulerThreadDaemon(), Thread.NORM_PRIORITY);
    }

    /**
     * <p>
     * Construct a new <code>QuartzSchedulerThread</code> for the given
     * <code>QuartzScheduler</code> as a <code>Thread</code> with the given
     * attributes.
     * </p>
     */
    QuartzSchedulerThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs, boolean setDaemon, int threadPrio) {
        super(qs.getSchedulerThreadGroup(), qsRsrcs.getThreadName());
        this.qs = qs;
        this.qsRsrcs = qsRsrcs;
        this.setDaemon(setDaemon);
        if (qsRsrcs.isThreadsInheritInitializersClassLoadContext()) {
            log.info("QuartzSchedulerThread Inheriting ContextClassLoader of thread: " + Thread.currentThread().getName());
            this.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        }
        this.setPriority(threadPrio);
        // start the underlying thread, but put this object into the 'paused'
        // state
        // so processing doesn't start yet...
        paused = true;
        halted = new AtomicBoolean(false);
    }

    void setIdleWaitTime(long waitTime) {
        idleWaitTime = waitTime;
        idleWaitVariablness = (int) (waitTime * 0.2);
    }

    private long getRandomizedIdleWaitTime() {
        return idleWaitTime - random.nextInt(idleWaitVariablness);
    }

    /**
     * Signals the main processing loop to pause at the next possible point.
     */
    void togglePause(boolean pause) {
        synchronized (sigLock) {
            paused = pause;
            if (paused) {
                signalSchedulingChange(0);
            } else {
                // 唤醒在sigLock上等待的所有线程
                sigLock.notifyAll();
            }
        }
    }

    /**
     * <p>
     * Signals the main processing loop to pause at the next possible point.
     * </p>
     * <p>
     * 停止，如果wait为true，表示等待当前任务执行完毕
     */
    void halt(boolean wait) {
        synchronized (sigLock) {
            halted.set(true);
            if (paused) {
                sigLock.notifyAll();
            } else {
                signalSchedulingChange(0);
            }
        }
        if (wait) {
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        join();
                        break;
                    } catch (InterruptedException _) {
                        interrupted = true;
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    boolean isPaused() {
        return paused;
    }

    /**
     * <p>
     * Signals the main processing loop that a change in scheduling has been
     * made - in order to interrupt any sleeping that may be occuring while
     * waiting for the fire time to arrive.
     * </p>
     *
     * @param candidateNewNextFireTime the time (in millis) when the newly scheduled trigger
     *                                 will fire.  If this method is being called do to some other even (rather
     *                                 than scheduling a trigger), the caller should pass zero (0).
     */
    public void signalSchedulingChange(long candidateNewNextFireTime) {
        synchronized (sigLock) {
            signaled = true;
            signaledNextFireTime = candidateNewNextFireTime;
            sigLock.notifyAll();
        }
    }

    public void clearSignaledSchedulingChange() {
        synchronized (sigLock) {
            signaled = false;
            signaledNextFireTime = 0;
        }
    }

    public boolean isScheduleChanged() {
        synchronized (sigLock) {
            return signaled;
        }
    }

    public long getSignaledNextFireTime() {
        synchronized (sigLock) {
            return signaledNextFireTime;
        }
    }

    /**
     * <p>
     * The main processing loop of the <code>QuartzSchedulerThread</code>.
     * <p>
     * https://www.jianshu.com/p/93afa50b1d7e
     * https://blog.csdn.net/qq_33265520/article/details/84639197
     * </p>
     */
    @Override
    public void run() {
        int acquiresFailed = 0;
        while (!halted.get()) {
            try {
                // -------------------------------
                // 1 等待QuartzScheduler启动
                // -------------------------------

                //循环检查paused && !halted.get()条件是否满足，否则释放sigLock对象的锁，并等待，一秒后重试。
                //当QuartzScheduler对象创建并调用start()方法时，将唤醒QuartzSchedulerThread线程，即可跳出阻塞块，继续执行。
                synchronized (sigLock) {
                    while (paused && !halted.get()) {
                        try {
                            // wait until togglePause(false) is called...
                            sigLock.wait(1000L);
                        } catch (InterruptedException ignore) {
                        }
                        // reset failure counter when paused, so that we don't
                        // wait again after unpausing
                        acquiresFailed = 0;
                    }
                    if (halted.get()) {
                        break;
                    }
                }

                // wait a bit, if reading from job store is consistently
                // failing (e.g. DB is down or restarting)..
                if (acquiresFailed > 1) {
                    try {
                        long delay = computeDelayForRepeatedErrors(qsRsrcs.getJobStore(), acquiresFailed);
                        Thread.sleep(delay);
                    } catch (Exception ignore) {
                    }
                }

                // -------------------------------
                // 2 查询待触发的Trigger
                // -------------------------------
                int availThreadCount = qsRsrcs.getThreadPool().blockForAvailableThreads();
                if (availThreadCount > 0) { // will always be true, due to semantics of blockForAvailableThreads...
                    List<OperableTrigger> triggers;
                    long now = System.currentTimeMillis();
                    clearSignaledSchedulingChange();
                    try {
                        // 查询未来（now + idletime）时间内待触发的Triggers
                        // triggers是按触发时间由近及远排序的集合
                        triggers = qsRsrcs.getJobStore().acquireNextTriggers(
                                now + idleWaitTime, Math.min(availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());
                        acquiresFailed = 0;
                        if (log.isDebugEnabled()) {
                            log.debug("batch acquisition of " + (triggers == null ? 0 : triggers.size()) + " triggers");
                        }
                    } catch (JobPersistenceException jpe) {
                        if (acquiresFailed == 0) {
                            qs.notifySchedulerListenersError(
                                    "An error occurred while scanning for the next triggers to fire.",
                                    jpe);
                        }
                        if (acquiresFailed < Integer.MAX_VALUE) {
                            acquiresFailed++;
                        }
                        continue;
                    } catch (RuntimeException e) {
                        if (acquiresFailed == 0) {
                            getLog().error("quartzSchedulerThreadLoop: RuntimeException "
                                    + e.getMessage(), e);
                        }
                        if (acquiresFailed < Integer.MAX_VALUE) {
                            acquiresFailed++;
                        }
                        continue;
                    }
                    if (triggers != null && !triggers.isEmpty()) {
                        now = System.currentTimeMillis();
                        long triggerTime = triggers.get(0).getNextFireTime().getTime();
                        long timeUntilTrigger = triggerTime - now;
                        // 通过循环阻塞，等待第一个Trigger触发时间
                        // 当触发时间距当前时间<=2 ms时，结束循环
                        //不过需要注意的是，在此期间，可能有一些新的情况发生，比如说，新增了一个Trigger，
                        // 并且该新增的Trigger比前面获取的触发时间都早，
                        // 那么就需要将上面获取的Trigger释放掉(状态变化:STATE_ACQUIRED-->STATE_WAITING)，
                        // 然后重新查询Trggers
                        while (timeUntilTrigger > 2) {
                            synchronized (sigLock) {
                                if (halted.get()) {
                                    break;
                                }
                                // 判断在此过程中是否有新增的并且触发时间更早的Trigger
                                // 但是此处有个权衡，为了一个新增的的Trigger而丢弃当前已获取的是否值得？
                                // 丢弃当前获取的Trigger并重新获取需要花费一定的时间，时间的长短与JobStore的实现有关。
                                // 所以此处做了主观判断，如果使用的是数据库存储，查询时间假定为70ms，内存存储假定为7ms
                                // 如果当前时间距已获得的第一个Trigger触发时间小于查询时间，则认为丢弃是不合算的。
                                if (!isCandidateNewTimeEarlierWithinReason(triggerTime, false)) {
                                    try {
                                        // we could have blocked a long while
                                        // on 'synchronize', so we must recompute
                                        now = System.currentTimeMillis();
                                        timeUntilTrigger = triggerTime - now;
                                        if (timeUntilTrigger >= 1) {
                                            sigLock.wait(timeUntilTrigger);
                                        }
                                    } catch (InterruptedException ignore) {
                                    }
                                }
                            }
                            // 如果有新增的且触发时间更早的Trigger过来搅局，则释放上面已获取的Trigger，等待下一波查询
                            if (releaseIfScheduleChangedSignificantly(triggers, triggerTime)) {
                                break;
                            }
                            now = System.currentTimeMillis();
                            timeUntilTrigger = triggerTime - now;
                        }
                        // this happens if releaseIfScheduleChangedSignificantly decided to release triggers
                        if (triggers.isEmpty()) {
                            continue;
                        }
                        // set triggers to 'executing'
                        List<TriggerFiredResult> bndles = new ArrayList<TriggerFiredResult>();
                        boolean goAhead = true;
                        synchronized (sigLock) {
                            goAhead = !halted.get();
                        }
                        if (goAhead) {
                            try {
                                // 通知JobStore，这些Triggers将要被触发
                                List<TriggerFiredResult> res = qsRsrcs.getJobStore().triggersFired(triggers);
                                if (res != null) {
                                    bndles = res;
                                }
                            } catch (SchedulerException se) {
                                qs.notifySchedulerListenersError(
                                        "An error occurred while firing triggers '"
                                                + triggers + "'", se);
                                //QTZ-179 : a problem occurred interacting with the triggers from the db
                                //we release them and loop again
                                for (int i = 0; i < triggers.size(); i++) {
                                    qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                }
                                continue;
                            }

                        }
                        // -------------------------------
                        // 3 触发Triggers
                        // -------------------------------
                        //前面提到过，先前只是获取Trigger的主要信息，
                        // 其关联的Job、Calendar等信息是在触发前获取的。
                        // 待Trigger所需信息验证、关联完成后，先行将Trigger的状态改为STATE_ACQUIRED-->STATE_COMPLETE。
                        // 而后将Trigger封装后的TriggerFiredResult对象交由JobRunShell执行
                        for (int i = 0; i < bndles.size(); i++) {
                            TriggerFiredResult result = bndles.get(i);
                            TriggerFiredBundle bndle = result.getTriggerFiredBundle();
                            Exception exception = result.getException();
                            if (exception instanceof RuntimeException) {
                                getLog().error("RuntimeException while firing trigger " + triggers.get(i), exception);
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }
                            // it's possible to get 'null' if the triggers was paused,
                            // blocked, or other similar occurrences that prevent it being
                            // fired at this time...  or if the scheduler was shutdown (halted)
                            if (bndle == null) {
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }
                            JobRunShell shell = null;
                            try {
                                shell = qsRsrcs.getJobRunShellFactory().createJobRunShell(bndle);
                                shell.initialize(qs);
                            } catch (SchedulerException se) {
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                                continue;
                            }
                            if (qsRsrcs.getThreadPool().runInThread(shell) == false) {
                                // this case should never happen, as it is indicative of the
                                // scheduler being shutdown or a bug in the thread pool or
                                // a thread pool being used concurrently - which the docs
                                // say not to do...
                                getLog().error("ThreadPool.runInThread() return false!");
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                            }

                        }
                        continue; // while (!halted)
                    }
                } else { // if(availThreadCount > 0)
                    // should never happen, if threadPool.blockForAvailableThreads() follows contract
                    continue; // while (!halted)
                }
                long now = System.currentTimeMillis();
                long waitTime = now + getRandomizedIdleWaitTime();
                long timeUntilContinue = waitTime - now;
                synchronized (sigLock) {
                    try {
                        if (!halted.get()) {
                            // QTZ-336 A job might have been completed in the mean time and we might have
                            // missed the scheduled changed signal by not waiting for the notify() yet
                            // Check that before waiting for too long in case this very job needs to be
                            // scheduled very soon
                            if (!isScheduleChanged()) {
                                sigLock.wait(timeUntilContinue);
                            }
                        }
                    } catch (InterruptedException ignore) {
                    }
                }

            } catch (RuntimeException re) {
                getLog().error("Runtime error occurred in main trigger firing loop.", re);
            }
        } // while (!halted)

        // drop references to scheduler stuff to aid garbage collection...
        qs = null;
        qsRsrcs = null;
    }

    /**
     * https://blog.csdn.net/qq_33265520/article/details/84639197
     * <p>
     * 讲解代码前先简要梳理run()方法的执行流程:
     * <p>
     * (1) 通过while循环检测调度是否应该停止，如果应该暂停则清空资源结束调度；
     * <p>
     * (2) 通过while循环检测调度是否应该暂停，如果应该暂停，则在循环中阻塞，直至被外部环境唤醒；
     * <p>
     * (3) 从线程池中查询可用的任务执行线程(WorkThread)，若线程池中暂时无可用的线程，则阻塞直到获取至少1个可用的线程；
     * <p>
     * (4) 根据一定的规则从任务存储区域(Job Store)中找出马上要执行的触发器(数据库存储每次取出一批触发器可减少与数据库的I/O操作)，每批触发器取出的数量(MaxBatchSize)和相应的时间窗参数（BatchTimeWindow）能够在配置文件中预设(默认情况下，这批触发器将在当前时间开始30秒内触发，且触发器的数量不小于当前可用的工作线程(availThreadCount)或在配置文件中设定好的阈值(MaxBatchSize)，该阈值默认为1，所以默认即使有多个触发器在同一时刻执行，该时刻每批也只读取一个触发器并逐批执行。)；
     * <p>
     * (5) 如果在第(4)步中没有发现需要执行的触发器，该线程阻塞一段时间（默认是23秒-30秒的随机时间），随后返回第（1）步；
     * <p>
     * (6) 取出的触发器将在内存中等待被执行，但在等待执行的过程中，外部环境也可能修改触发器从而影响触发的时间。所以线程会阻塞到预定将被触发的时间，若阻塞未被打断，则执行若触发受到外部环境影响，将根据一定的条件判断是否重新取出一批触发器，如果是，则抛弃现有的触发器，回到第(1)步；
     * <p>
     * (7) 执行到第(6)步结束后，程序根据触发器取出对应的任务(job)、记录触发器的触发信息，并调整触发器下一次触发的时间，使触发器在下一次触发时能被取出；
     * <p>
     * (8) 执行所有取出的触发器；
     * <p>
     * (9) 执行完所有的触发任务后返回第(1)步，重新取出下一批触发器。
     */
    public void run2() {
        int acquiresFailed = 0;
        //***判断是否应该结束调度
        while (!halted.get()) {
            try {
                //**********这里的sigLock，sig应该是signal(信号)的简写
                synchronized (sigLock) {
                    //**********判断是否应该暂停调度，如果暂停则不断在循环中阻塞
                    //**********如暂停状态被外部环境修改，则线程会被立即唤醒并退出循环
                    while (paused && !halted.get()) {
                        try {
                            // wait until togglePause(false) is called...
                            sigLock.wait(1000L);
                        } catch (InterruptedException ignore) {
                        }
                        // reset failure counter when paused, so that we don't
                        // wait again after unpausing
                        acquiresFailed = 0;
                    }
                    if (halted.get()) {
                        break;
                    }
                }

                // wait a bit, if reading from job store is consistently
                // failing (e.g. DB is down or restarting)..
                //**********在前几次的循环中如果触发器的读取出现问题，
                //**********则可能是数据库重启一类的原因引发的故障
                if (acquiresFailed > 1) {
                    try {
                        long delay = computeDelayForRepeatedErrors(qsRsrcs.getJobStore(), acquiresFailed);
                        Thread.sleep(delay);
                    } catch (Exception ignore) {
                    }
                }

                //**********查询可用于执行任务(job)的工作线程数量，
                //**********若线程池中暂无可用线程则blockForAvailableThreads方法将会阻塞
                int availThreadCount = qsRsrcs.getThreadPool().blockForAvailableThreads();
                if (availThreadCount > 0) { // will always be true, due to semantics of blockForAvailableThreads...
                    //**********这个if分支查询到可用的工作线程，从JobStore中获取一批即将执行的触发器
                    //**********这里的JobStore存储介质可以是数据库、也可以是内存
                    List<OperableTrigger> triggers;
                    long now = System.currentTimeMillis();
                    clearSignaledSchedulingChange();
                    try {
                        triggers = qsRsrcs.getJobStore().acquireNextTriggers(
                                now + idleWaitTime, Math.min(availThreadCount, qsRsrcs.getMaxBatchSize()), qsRsrcs.getBatchTimeWindow());
                        //**********acquireNextTriggers方法获取一批即将执行的触发器
                        //**********参数idleWaitTime默认为30s,即当前时间后30s内即将被触发执行的触发器就会被取出
                        //**********此外在acquireNextTriggers方法内部还有一个参数misfireThreshold
                        //**********misfireThreshold是一个时间范围，用于判定触发器是否延时触发
                        //**********misfireThreshold默认值是60秒，它相对的实际意义就是:
                        //**********在当前时间的60秒之前本应执行但尚未执行的触发器不被认为是延迟触发,
                        //**********这些触发器同样会被acquireNextTriggers发现
                        //**********有时由于工程线程繁忙、程序重启等原因，原本预定要触发的任务可能延迟
                        //**********我们可以在每个触发器中可以设置MISFIRE_INSTRUCTION,用于指定延迟触发后使用的策略
                        //**********举例，对于CronTrigger,延迟处理的策略主要有3种：
                        //**********（1）一个触发器无论延迟多少次，这些延迟都会被程序尽可能补回来
                        //**********（2）检测到触发器延迟后，该触发器会在尽可能短的时间内被立即执行一次(只有一次)，然后恢复正常
                        //**********（3）检测到延迟后不采取任何动作，触发器以现在时间为基准，根据自身的安排等待下一次被执行或停止，
                        //**********     比如有些触发器只执行一次，一旦延迟后，该触发器也不会被触发


                        //**********关于触发器是否延迟的判定由一个叫MisfireHandler的线程独立负责，
                        //**********它会判定并影响延迟触发器的下一次触发，但不会真正进行触发的动作，
                        //**********触发的工作将统一交由QuartzSchedulerThread即本线程处理
                        //**********如果判定一个触发器延迟，则根据策略修改触发器的下一次执行时间或直接停止触发器
                        //**********所以这些延迟触发器被MisfireHandler处理后若仍有下次执行机会，就同样会在其触发时间被发现并触发
                        //**********需要注意的是MisfireHandler只会处理延迟策略不为上述第(1)类的触发器
                        //**********第(1)类触发器在延迟后，一旦获取到资源就可触发，这个过程不需被修改下次执行时间就可完成


                        //**********acquireNextTriggers方法最后一个参数batchTimeWindow，这个参数默认是0，同样是一个时间范围
                        //**********acquireNextTriggers可以每次取出一批触发器，但默认情况下这批触发器只会有一个
                        //**********但是有时候我们对任务执行的时间要求不严格时，就可以让两个执行时间距离较近的触发器同时被取出执行

                        //**********举例，有两个触发器分别是10:00:00和10:00:05执行
                        //**********此时如果将batchTimeWindow调整为大于等于5000毫秒，maxBatchSize数量大于等于2，
                        //**********且拥有足够的线程时,这两个触发器就有可能会在预定时间10:00:00被同时执行
                        acquiresFailed = 0;
                        if (log.isDebugEnabled())
                            log.debug("batch acquisition of " + (triggers == null ? 0 : triggers.size()) + " triggers");
                    } catch (JobPersistenceException jpe) {
                        if (acquiresFailed == 0) {
                            qs.notifySchedulerListenersError(
                                    "An error occurred while scanning for the next triggers to fire.",
                                    jpe);
                        }
                        if (acquiresFailed < Integer.MAX_VALUE)
                            acquiresFailed++;
                        continue;
                    } catch (RuntimeException e) {
                        if (acquiresFailed == 0) {
                            getLog().error("quartzSchedulerThreadLoop: RuntimeException "
                                    + e.getMessage(), e);
                        }
                        if (acquiresFailed < Integer.MAX_VALUE)
                            acquiresFailed++;
                        continue;
                    }

                    if (triggers != null && !triggers.isEmpty()) {

                        now = System.currentTimeMillis();
                        long triggerTime = triggers.get(0).getNextFireTime().getTime();
                        long timeUntilTrigger = triggerTime - now;

                        while (timeUntilTrigger > 2) {
                            //**********在该while循环体中，被取出的触发器会阻塞等待到预定时间被触发
                            //**********这里用了阻塞，因为当外部环境对触发器做了调整或者新增时，会对线程进行唤醒
                            //**********在阻塞被唤醒后，会有相关的逻辑判断是否应该重新取出触发器来执行
                            //**********比如当前时间是10:00:00，在上述逻辑中已经取出了10:00:05需要执行的触发器
                            //**********此时如果新增了一个10:00:03的触发器，则可能需要丢弃10:00:05的，再取出10:00:03的
                            synchronized (sigLock) {
                                if (halted.get()) {
                                    break;
                                }
                                if (!isCandidateNewTimeEarlierWithinReason(triggerTime, false)) {
                                    try {
                                        // we could have blocked a long while
                                        // on 'synchronize', so we must recompute
                                        now = System.currentTimeMillis();
                                        timeUntilTrigger = triggerTime - now;
                                        if (timeUntilTrigger >= 1)
                                            sigLock.wait(timeUntilTrigger);
                                    } catch (InterruptedException ignore) {
                                    }
                                }
                            }
                            if (releaseIfScheduleChangedSignificantly(triggers, triggerTime)) {
                                break;
                            }
                            now = System.currentTimeMillis();
                            timeUntilTrigger = triggerTime - now;
                        }

                        // this happens if releaseIfScheduleChangedSignificantly decided to release triggers
                        if (triggers.isEmpty())
                            continue;

                        // set triggers to 'executing'
                        List<TriggerFiredResult> bndles = new ArrayList<TriggerFiredResult>();

                        boolean goAhead = true;
                        synchronized (sigLock) {
                            goAhead = !halted.get();
                        }
                        if (goAhead) {
                            try {
                                //**********triggersFired方法主要有几个作用:
                                //**********(1)取出触发器对应应执行的任务
                                //**********(2)记录触发器的执行，修改触发器的状态，如果对应的任务是StatefulJob，则阻塞其他触发器
                                //**********(3)调整触发器下次执行的时间
                                List<TriggerFiredResult> res = qsRsrcs.getJobStore().triggersFired(triggers);
                                if (res != null)
                                    bndles = res;
                            } catch (SchedulerException se) {
                                qs.notifySchedulerListenersError(
                                        "An error occurred while firing triggers '"
                                                + triggers + "'", se);
                                //QTZ-179 : a problem occurred interacting with the triggers from the db
                                //we release them and loop again
                                for (int i = 0; i < triggers.size(); i++) {
                                    qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                }
                                continue;
                            }

                        }

                        for (int i = 0; i < bndles.size(); i++) {
                            //**********这个循环就是将当前取出的触发器挨个执行，并触发相应的监听器
                            TriggerFiredResult result = bndles.get(i);
                            TriggerFiredBundle bndle = result.getTriggerFiredBundle();
                            Exception exception = result.getException();

                            if (exception instanceof RuntimeException) {
                                getLog().error("RuntimeException while firing trigger " + triggers.get(i), exception);
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }

                            // it's possible to get 'null' if the triggers was paused,
                            // blocked, or other similar occurrences that prevent it being
                            // fired at this time...  or if the scheduler was shutdown (halted)
                            if (bndle == null) {
                                qsRsrcs.getJobStore().releaseAcquiredTrigger(triggers.get(i));
                                continue;
                            }

                            JobRunShell shell = null;
                            try {
                                shell = qsRsrcs.getJobRunShellFactory().createJobRunShell(bndle);
                                shell.initialize(qs);
                            } catch (SchedulerException se) {
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                                continue;
                            }
                            //**********从线程池中取出线程执行任务
                            if (qsRsrcs.getThreadPool().runInThread(shell) == false) {
                                // this case should never happen, as it is indicative of the
                                // scheduler being shutdown or a bug in the thread pool or
                                // a thread pool being used concurrently - which the docs
                                // say not to do...
                                getLog().error("ThreadPool.runInThread() return false!");
                                qsRsrcs.getJobStore().triggeredJobComplete(triggers.get(i), bndle.getJobDetail(), CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR);
                            }

                        }
                        //**********执行完后重新再取下一批触发器
                        continue; // while (!halted)
                    }
                } else { // if(availThreadCount > 0)
                    // should never happen, if threadPool.blockForAvailableThreads() follows contract
                    continue; // while (!halted)
                }
                //**********若本次循环未取出触发器，则阻塞一段时间(随机时间)，然后再重试
                long now = System.currentTimeMillis();
                long waitTime = now + getRandomizedIdleWaitTime();
                long timeUntilContinue = waitTime - now;
                synchronized (sigLock) {
                    try {
                        if (!halted.get()) {
                            // QTZ-336 A job might have been completed in the mean time and we might have
                            // missed the scheduled changed signal by not waiting for the notify() yet
                            // Check that before waiting for too long in case this very job needs to be
                            // scheduled very soon
                            if (!isScheduleChanged()) {
                                sigLock.wait(timeUntilContinue);
                            }
                        }
                    } catch (InterruptedException ignore) {
                    }
                }

            } catch (RuntimeException re) {
                getLog().error("Runtime error occurred in main trigger firing loop.", re);
            }
        } // while (!halted)

        // drop references to scheduler stuff to aid garbage collection...
        qs = null;
        qsRsrcs = null;
    }

    private static final long MIN_DELAY = 20;
    private static final long MAX_DELAY = 600000;

    private static long computeDelayForRepeatedErrors(JobStore jobStore, int acquiresFailed) {
        long delay;
        try {
            delay = jobStore.getAcquireRetryDelay(acquiresFailed);
        } catch (Exception ignored) {
            // we're trying to be useful in case of error states, not cause
            // additional errors..
            delay = 100;
        }
        // sanity check per getAcquireRetryDelay specification
        if (delay < MIN_DELAY) {
            delay = MIN_DELAY;
        }
        if (delay > MAX_DELAY) {
            delay = MAX_DELAY;
        }

        return delay;
    }

    private boolean releaseIfScheduleChangedSignificantly(
            List<OperableTrigger> triggers, long triggerTime) {
        if (isCandidateNewTimeEarlierWithinReason(triggerTime, true)) {
            // above call does a clearSignaledSchedulingChange()
            for (OperableTrigger trigger : triggers) {
                qsRsrcs.getJobStore().releaseAcquiredTrigger(trigger);
            }
            triggers.clear();
            return true;
        }
        return false;
    }

    private boolean isCandidateNewTimeEarlierWithinReason(long oldTime, boolean clearSignal) {

        // So here's the deal: We know due to being signaled that 'the schedule'
        // has changed.  We may know (if getSignaledNextFireTime() != 0) the
        // new earliest fire time.  We may not (in which case we will assume
        // that the new time is earlier than the trigger we have acquired).
        // In either case, we only want to abandon our acquired trigger and
        // go looking for a new one if "it's worth it".  It's only worth it if
        // the time cost incurred to abandon the trigger and acquire a new one
        // is less than the time until the currently acquired trigger will fire,
        // otherwise we're just "thrashing" the job store (e.g. database).
        //
        // So the question becomes when is it "worth it"?  This will depend on
        // the job store implementation (and of course the particular database
        // or whatever behind it).  Ideally we would depend on the job store
        // implementation to tell us the amount of time in which it "thinks"
        // it can abandon the acquired trigger and acquire a new one.  However
        // we have no current facility for having it tell us that, so we make
        // a somewhat educated but arbitrary guess ;-).

        synchronized (sigLock) {
            if (!isScheduleChanged()) {
                return false;
            }
            boolean earlier = false;
            if (getSignaledNextFireTime() == 0) {
                earlier = true;
            } else if (getSignaledNextFireTime() < oldTime) {
                earlier = true;
            }

            if (earlier) {
                // so the new time is considered earlier, but is it enough earlier?
                long diff = oldTime - System.currentTimeMillis();
                if (diff < (qsRsrcs.getJobStore().supportsPersistence() ? 70L : 7L)) {
                    earlier = false;
                }
            }
            if (clearSignal) {
                clearSignaledSchedulingChange();
            }
            return earlier;
        }
    }

    public Logger getLog() {
        return log;
    }

}
