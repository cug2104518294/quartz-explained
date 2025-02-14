package org.quartz.listeners;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds a List of references to TriggerListener instances and broadcasts all
 * events to them (in order).
 *
 * <p>The broadcasting behavior of this listener to delegate listeners may be
 * more convenient than registering all of the listeners directly with the
 * Scheduler, and provides the flexibility of easily changing which listeners
 * get notified.</p>
 *
 * @author James House (jhouse AT revolition DOT net)
 * @see #addListener(org.quartz.TriggerListener)
 * @see #removeListener(org.quartz.TriggerListener)
 * @see #removeListener(String)
 */
public class BroadcastTriggerListener implements TriggerListener {

    private String name;
    private List<TriggerListener> listeners;

    /**
     * Construct an instance with the given name.
     * <p>
     * (Remember to add some delegate listeners!)
     *
     * @param name the name of this instance
     */
    public BroadcastTriggerListener(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Listener name cannot be null!");
        }
        this.name = name;
        listeners = new LinkedList<TriggerListener>();
    }

    /**
     * Construct an instance with the given name, and List of listeners.
     *
     * @param name      the name of this instance
     * @param listeners the initial List of TriggerListeners to broadcast to.
     */
    public BroadcastTriggerListener(String name, List<TriggerListener> listeners) {
        this(name);
        this.listeners.addAll(listeners);
    }

    @Override
    public String getName() {
        return name;
    }

    public void addListener(TriggerListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(TriggerListener listener) {
        return listeners.remove(listener);
    }

    public boolean removeListener(String listenerName) {
        Iterator<TriggerListener> itr = listeners.iterator();
        while (itr.hasNext()) {
            TriggerListener l = itr.next();
            if (l.getName().equals(listenerName)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public List<TriggerListener> getListeners() {
        return java.util.Collections.unmodifiableList(listeners);
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {

        Iterator<TriggerListener> itr = listeners.iterator();
        while (itr.hasNext()) {
            TriggerListener l = itr.next();
            l.triggerFired(trigger, context);
        }
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {

        Iterator<TriggerListener> itr = listeners.iterator();
        while (itr.hasNext()) {
            TriggerListener l = itr.next();
            if (l.vetoJobExecution(trigger, context)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {

        Iterator<TriggerListener> itr = listeners.iterator();
        while (itr.hasNext()) {
            TriggerListener l = itr.next();
            l.triggerMisfired(trigger);
        }
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {

        Iterator<TriggerListener> itr = listeners.iterator();
        while (itr.hasNext()) {
            TriggerListener l = itr.next();
            l.triggerComplete(trigger, context, triggerInstructionCode);
        }
    }

}
