package org.quartz.impl.jdbcjobstore;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;

import java.sql.Connection;

/**
 * <p>
 * <code>JobStoreTX</code> is meant to be used in a standalone environment.
 * Both commit and rollback will be handled by this class.
 * </p>
 *
 * <p>
 * If you need a <code>{@link org.quartz.spi.JobStore}</code> class to use
 * within an application-server environment, use <code>{@link
 * org.quartz.impl.jdbcjobstore.JobStoreCMT}</code>
 * instead.
 * </p>
 * <p>
 * 事务的选择：
 * - 如果你不需要将调度命令（如增加和删除 trigger）与其它事务绑定，可以让 Quartz 通过JobStoreTX来管理事务（这
 * 是最常用的选择）。
 * - 如果你需要 Quartz 与其它事务一起工作（比如在 J2EE 应用中），你应该使用JobStoreCMT - 在这种情况下，
 * Quartz 会让应用服务器管理事务。
 *
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 * @author James House
 */
public class JobStoreTX extends JobStoreSupport {

    @Override
    public void initialize(ClassLoadHelper classLoadHelper,
                           SchedulerSignaler schedSignaler) throws SchedulerConfigException {

        super.initialize(classLoadHelper, schedSignaler);

        getLog().info("JobStoreTX initialized.");
    }

    /**
     * For <code>JobStoreTX</code>, the non-managed TX connection is just
     * the normal connection because it is not CMT.
     *
     * @see JobStoreSupport#getConnection()
     */
    @Override
    protected Connection getNonManagedTXConnection()
            throws JobPersistenceException {
        return getConnection();
    }

    /**
     * Execute the given callback having optionally aquired the given lock.
     * For <code>JobStoreTX</code>, because it manages its own transactions
     * and only has the one datasource, this is the same behavior as
     * executeInNonManagedTXLock().
     *
     * @param lockName The name of the lock to aquire, for example
     *                 "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     *                 lockCallback is still executed in a transaction.
     * @see JobStoreSupport#executeInNonManagedTXLock(String, TransactionCallback)
     * @see JobStoreCMT#executeInLock(String, TransactionCallback)
     * @see JobStoreSupport#getNonManagedTXConnection()
     * @see JobStoreSupport#getConnection()
     */
    @Override
    protected Object executeInLock(
            String lockName,
            TransactionCallback txCallback) throws JobPersistenceException {
        return executeInNonManagedTXLock(lockName, txCallback, null);
    }
}
// EOF
