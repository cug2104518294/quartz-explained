package org.quartz.core;

import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;

import java.util.*;

public class ListenerManagerImpl implements ListenerManager {

    private Map<String, JobListener> globalJobListeners = new LinkedHashMap<String, JobListener>(10);

    private Map<String, TriggerListener> globalTriggerListeners = new LinkedHashMap<String, TriggerListener>(10);

    private Map<String, List<Matcher<JobKey>>> globalJobListenersMatchers = new LinkedHashMap<String, List<Matcher<JobKey>>>(10);

    private Map<String, List<Matcher<TriggerKey>>> globalTriggerListenersMatchers = new LinkedHashMap<String, List<Matcher<TriggerKey>>>(10);

    private ArrayList<SchedulerListener> schedulerListeners = new ArrayList<SchedulerListener>(10);


    @Override
    public void addJobListener(JobListener jobListener, Matcher<JobKey>... matchers) {
        addJobListener(jobListener, Arrays.asList(matchers));
    }

    @Override
    public void addJobListener(JobListener jobListener, List<Matcher<JobKey>> matchers) {
        if (jobListener.getName() == null || jobListener.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "JobListener name cannot be empty.");
        }

        synchronized (globalJobListeners) {
            globalJobListeners.put(jobListener.getName(), jobListener);
            LinkedList<Matcher<JobKey>> matchersL = new LinkedList<Matcher<JobKey>>();
            if (matchers != null && matchers.size() > 0)
                matchersL.addAll(matchers);
            else
                matchersL.add(EverythingMatcher.allJobs());

            globalJobListenersMatchers.put(jobListener.getName(), matchersL);
        }
    }


    @Override
    public void addJobListener(JobListener jobListener) {
        addJobListener(jobListener, EverythingMatcher.allJobs());
    }

    @Override
    public void addJobListener(JobListener jobListener, Matcher<JobKey> matcher) {
        if (jobListener.getName() == null || jobListener.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "JobListener name cannot be empty.");
        }

        synchronized (globalJobListeners) {
            globalJobListeners.put(jobListener.getName(), jobListener);
            LinkedList<Matcher<JobKey>> matchersL = new LinkedList<Matcher<JobKey>>();
            if (matcher != null)
                matchersL.add(matcher);
            else
                matchersL.add(EverythingMatcher.allJobs());

            globalJobListenersMatchers.put(jobListener.getName(), matchersL);
        }
    }


    @Override
    public boolean addJobListenerMatcher(String listenerName, Matcher<JobKey> matcher) {
        if (matcher == null)
            throw new IllegalArgumentException("Null value not acceptable.");

        synchronized (globalJobListeners) {
            List<Matcher<JobKey>> matchers = globalJobListenersMatchers.get(listenerName);
            if (matchers == null)
                return false;
            matchers.add(matcher);
            return true;
        }
    }

    public boolean removeJobListenerMatcher(String listenerName, Matcher<JobKey> matcher) {
        if (matcher == null)
            throw new IllegalArgumentException("Non-null value not acceptable.");

        synchronized (globalJobListeners) {
            List<Matcher<JobKey>> matchers = globalJobListenersMatchers.get(listenerName);
            if (matchers == null)
                return false;
            return matchers.remove(matcher);
        }
    }

    @Override
    public List<Matcher<JobKey>> getJobListenerMatchers(String listenerName) {
        synchronized (globalJobListeners) {
            List<Matcher<JobKey>> matchers = globalJobListenersMatchers.get(listenerName);
            if (matchers == null)
                return null;
            return Collections.unmodifiableList(matchers);
        }
    }

    public boolean setJobListenerMatchers(String listenerName, List<Matcher<JobKey>> matchers) {
        if (matchers == null)
            throw new IllegalArgumentException("Non-null value not acceptable.");

        synchronized (globalJobListeners) {
            List<Matcher<JobKey>> oldMatchers = globalJobListenersMatchers.get(listenerName);
            if (oldMatchers == null)
                return false;
            globalJobListenersMatchers.put(listenerName, matchers);
            return true;
        }
    }


    @Override
    public boolean removeJobListener(String name) {
        synchronized (globalJobListeners) {
            return (globalJobListeners.remove(name) != null);
        }
    }

    @Override
    public List<JobListener> getJobListeners() {
        synchronized (globalJobListeners) {
            return java.util.Collections.unmodifiableList(new LinkedList<JobListener>(globalJobListeners.values()));
        }
    }

    public JobListener getJobListener(String name) {
        synchronized (globalJobListeners) {
            return globalJobListeners.get(name);
        }
    }

    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey>... matchers) {
        addTriggerListener(triggerListener, Arrays.asList(matchers));
    }

    @Override
    public void addTriggerListener(TriggerListener triggerListener, List<Matcher<TriggerKey>> matchers) {
        if (triggerListener.getName() == null
                || triggerListener.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "TriggerListener name cannot be empty.");
        }

        synchronized (globalTriggerListeners) {
            globalTriggerListeners.put(triggerListener.getName(), triggerListener);

            LinkedList<Matcher<TriggerKey>> matchersL = new LinkedList<Matcher<TriggerKey>>();
            if (matchers != null && matchers.size() > 0)
                matchersL.addAll(matchers);
            else
                matchersL.add(EverythingMatcher.allTriggers());

            globalTriggerListenersMatchers.put(triggerListener.getName(), matchersL);
        }
    }

    @Override
    public void addTriggerListener(TriggerListener triggerListener) {
        addTriggerListener(triggerListener, EverythingMatcher.allTriggers());
    }

    @Override
    public void addTriggerListener(TriggerListener triggerListener, Matcher<TriggerKey> matcher) {
        if (matcher == null)
            throw new IllegalArgumentException("Null value not acceptable for matcher.");

        if (triggerListener.getName() == null
                || triggerListener.getName().length() == 0) {
            throw new IllegalArgumentException(
                    "TriggerListener name cannot be empty.");
        }

        synchronized (globalTriggerListeners) {
            globalTriggerListeners.put(triggerListener.getName(), triggerListener);
            List<Matcher<TriggerKey>> matchers = new LinkedList<Matcher<TriggerKey>>();
            matchers.add(matcher);
            globalTriggerListenersMatchers.put(triggerListener.getName(), matchers);
        }
    }

    @Override
    public boolean addTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher) {
        if (matcher == null)
            throw new IllegalArgumentException("Non-null value not acceptable.");

        synchronized (globalTriggerListeners) {
            List<Matcher<TriggerKey>> matchers = globalTriggerListenersMatchers.get(listenerName);
            if (matchers == null)
                return false;
            matchers.add(matcher);
            return true;
        }
    }

    public boolean removeTriggerListenerMatcher(String listenerName, Matcher<TriggerKey> matcher) {
        if (matcher == null)
            throw new IllegalArgumentException("Non-null value not acceptable.");

        synchronized (globalTriggerListeners) {
            List<Matcher<TriggerKey>> matchers = globalTriggerListenersMatchers.get(listenerName);
            if (matchers == null)
                return false;
            return matchers.remove(matcher);
        }
    }

    @Override
    public List<Matcher<TriggerKey>> getTriggerListenerMatchers(String listenerName) {
        synchronized (globalTriggerListeners) {
            List<Matcher<TriggerKey>> matchers = globalTriggerListenersMatchers.get(listenerName);
            if (matchers == null)
                return null;
            return Collections.unmodifiableList(matchers);
        }
    }

    public boolean setTriggerListenerMatchers(String listenerName, List<Matcher<TriggerKey>> matchers) {
        if (matchers == null)
            throw new IllegalArgumentException("Non-null value not acceptable.");

        synchronized (globalTriggerListeners) {
            List<Matcher<TriggerKey>> oldMatchers = globalTriggerListenersMatchers.get(listenerName);
            if (oldMatchers == null)
                return false;
            globalTriggerListenersMatchers.put(listenerName, matchers);
            return true;
        }
    }

    @Override
    public boolean removeTriggerListener(String name) {
        synchronized (globalTriggerListeners) {
            return (globalTriggerListeners.remove(name) != null);
        }
    }


    @Override
    public List<TriggerListener> getTriggerListeners() {
        synchronized (globalTriggerListeners) {
            return java.util.Collections.unmodifiableList(new LinkedList<TriggerListener>(globalTriggerListeners.values()));
        }
    }

    public TriggerListener getTriggerListener(String name) {
        synchronized (globalTriggerListeners) {
            return globalTriggerListeners.get(name);
        }
    }


    @Override
    public void addSchedulerListener(SchedulerListener schedulerListener) {
        synchronized (schedulerListeners) {
            schedulerListeners.add(schedulerListener);
        }
    }

    @Override
    public boolean removeSchedulerListener(SchedulerListener schedulerListener) {
        synchronized (schedulerListeners) {
            return schedulerListeners.remove(schedulerListener);
        }
    }

    @Override
    public List<SchedulerListener> getSchedulerListeners() {
        synchronized (schedulerListeners) {
            return java.util.Collections.unmodifiableList(new ArrayList<SchedulerListener>(schedulerListeners));
        }
    }
}
