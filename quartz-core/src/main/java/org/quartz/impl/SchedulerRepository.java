package org.quartz.impl;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.Collection;
import java.util.HashMap;

/**
 * <p>
 * Holds references to Scheduler instances - ensuring uniqueness, and
 * preventing garbage collection, and allowing 'global' lookups - all within a
 * ClassLoader space.
 * </p>
 *
 * <p>
 * 保存scheduler实例，单例模式，主要作用：
 * - 保证唯一性
 * - 禁止gc
 * - 通过name全局查找
 * </p>
 **/
public class SchedulerRepository {

    private HashMap<String, Scheduler> schedulers;

    private static SchedulerRepository inst;

    private SchedulerRepository() {
        schedulers = new HashMap<>();
    }

    //单例模式：通过synchronized保证线程安全
    public static synchronized SchedulerRepository getInstance() {
        if (inst == null) {
            inst = new SchedulerRepository();
        }
        return inst;
    }

    public synchronized void bind(Scheduler sched) throws SchedulerException {
        if (schedulers.get(sched.getSchedulerName()) != null) {
            throw new SchedulerException("Scheduler with name '"
                    + sched.getSchedulerName() + "' already exists.");
        }
        schedulers.put(sched.getSchedulerName(), sched);
    }

    public synchronized boolean remove(String schedName) {
        return (schedulers.remove(schedName) != null);
    }

    public synchronized Scheduler lookup(String schedName) {
        return schedulers.get(schedName);
    }


    /**
     * 只读集合
     * <p>
     * Collections提供了生成几种生成只读集合的方法unmodifiableCollection，unmodifiableList，unmodifiableMap，unmodifiableSet，
     * unmodifiableSortedMap，unmodifiableSortedSet。这些集合一旦初始化以后就不能修改，任何修改这些集合的方法都会抛出
     * UnsupportedOperationException异常。
     */
    public synchronized Collection<Scheduler> lookupAll() {
        return java.util.Collections
                .unmodifiableCollection(schedulers.values());
    }
}