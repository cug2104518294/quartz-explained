package org.quartz;

import java.lang.annotation.*;

/**
 * An annotation that marks a {@link Job} class as one that must not have multiple
 * instances executed concurrently (where instance is based-upon a {@link JobDetail}
 * definition - or in other words based upon a {@link JobKey}).
 *
 * @author jhouse
 * @see PersistJobDataAfterExecution
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisallowConcurrentExecution {

}
