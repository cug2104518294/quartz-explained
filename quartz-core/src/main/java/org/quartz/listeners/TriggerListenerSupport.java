package org.quartz.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.TriggerListener;
import org.quartz.Trigger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger.CompletedExecutionInstruction;

/**
 * A helpful abstract base class for implementors of 
 * <code>{@link org.quartz.TriggerListener}</code>.
 * 
 * <p>
 * The methods in this class are empty so you only need to override the  
 * subset for the <code>{@link org.quartz.TriggerListener}</code> events
 * you care about.
 * </p>
 * 
 * <p>
 * You are required to implement <code>{@link org.quartz.TriggerListener#getName()}</code> 
 * to return the unique name of your <code>TriggerListener</code>.  
 * </p>
 *
 * TriggerListener接口的抽象方法，提供接口中方法的默认实现，可以继承该方法，重写感兴趣的方法即可。
 * 但是{@link org.quartz.TriggerListener#getName()}需要实现，返回listener的唯一方法。
 * 
 * @see org.quartz.TriggerListener
 */
public abstract class TriggerListenerSupport implements TriggerListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get the <code>{@link org.slf4j.Logger}</code> for this
     * class's category.  This should be used by subclasses for logging.
     */
    protected Logger getLog() {
        return log;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
    }

    @Override
    public void triggerComplete(
        Trigger trigger,
        JobExecutionContext context,
        CompletedExecutionInstruction triggerInstructionCode) {
    }
}
