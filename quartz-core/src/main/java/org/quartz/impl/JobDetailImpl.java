package org.quartz.impl;

import org.quartz.*;
import org.quartz.utils.ClassUtils;

/**
 * <p>
 * Conveys the detail properties of a given <code>Job</code> instance.
 * </p>
 *
 * <p>
 * Quartz does not store an actual instance of a <code>Job</code> class, but
 * instead allows you to define an instance of one, through the use of a <code>JobDetail</code>.
 * </p>
 *
 * <p>
 * <code>Job</code>s have a name and group associated with them, which
 * should uniquely identify them within a single <code>{@link Scheduler}</code>.
 * </p>
 *
 * <p>
 * <code>Trigger</code>s are the 'mechanism' by which <code>Job</code>s
 * are scheduled. Many <code>Trigger</code>s can point to the same <code>Job</code>,
 * but a single <code>Trigger</code> can only point to one <code>Job</code>.
 * </p>
 *
 * <p>
 * JobDetail中的数据为Job的属性，其中name和group是job在一个scheduler中的唯一标识
 * </p>
 *
 * @author James House
 * @author Sharada Jambula
 * @see Job
 * @see StatefulJob
 * @see JobDataMap
 * @see Trigger
 */
@SuppressWarnings("deprecation")
public class JobDetailImpl implements Cloneable, java.io.Serializable, JobDetail {

    private static final long serialVersionUID = -6069784757781506897L;

    /**
     * job名称。如果未指定，会自动分配一个唯一名称。
     * 所有job都必须拥有一个唯一name，如果两个job的name重复，则只有最前面的job能被调度
     */
    private String name;

    private String group = Scheduler.DEFAULT_GROUP;

    private String description;

    /**
     * 必须是job实现类（比如JobImpl），用来绑定一个具体job
     */
    private Class<? extends Job> jobClass;

    /**
     * 除了上面常规属性外，用户可以把任意kv数据存入jobDataMap，实现job属性的无限制扩展，执行job时可以使用这些属性数据。
     * 此属性的类型是JobDataMap，实现了Serializable接口，可做跨平台的序列化传输
     */
    private JobDataMap jobDataMap;

    /**
     * 是否持久化。
     * 如果job设置为非持久，当没有活跃的trigger与之关联的时候，job会自动从scheduler中删除。
     * 也就是说，非持久job的生命期是由trigger的存在与否决定的
     */
    private boolean durability = false;

    /**
     * 是否可恢复。
     * 如果job设置为可恢复，一旦job执行时scheduler发生hard shutdown（比如进程崩溃或关机）
     * 当scheduler重启后，该job会被重新执行
     */
    private boolean shouldRecover = false;

    private transient JobKey key = null;

    /**
     * <p>
     * Create a <code>JobDetail</code> with no specified name or group, and
     * the default settings of all the other properties.
     * </p>
     *
     * <p>
     * Note that the {@link #setName(String)},{@link #setGroup(String)}and
     * {@link #setJobClass(Class)}methods must be called before the job can be
     * placed into a {@link Scheduler}
     * </p>
     */
    public JobDetailImpl() {
        // do nothing...
    }

    /**
     * <p>
     * Create a <code>JobDetail</code> with the given name, given class, default group,
     * and the default settings of all the other properties.
     * </p>
     *
     * @throws IllegalArgumentException if name is null or empty, or the group is an empty string.
     * @deprecated use {@link JobBuilder}
     */
    public JobDetailImpl(String name, Class<? extends Job> jobClass) {
        this(name, null, jobClass);
    }

    /**
     * <p>
     * Create a <code>JobDetail</code> with the given name, group and class,
     * and the default settings of all the other properties.
     * </p>
     *
     * @param group if <code>null</code>, Scheduler.DEFAULT_GROUP will be used.
     * @throws IllegalArgumentException if name is null or empty, or the group is an empty string.
     * @deprecated use {@link JobBuilder}
     */
    public JobDetailImpl(String name, String group, Class<? extends Job> jobClass) {
        setName(name);
        setGroup(group);
        setJobClass(jobClass);
    }

    /**
     * <p>
     * Create a <code>JobDetail</code> with the given name, and group, and
     * the given settings of all the other properties.
     * </p>
     *
     * @param group if <code>null</code>, Scheduler.DEFAULT_GROUP will be used.
     * @throws IllegalArgumentException if name is null or empty, or the group is an empty string.
     * @deprecated use {@link JobBuilder}
     */
    public JobDetailImpl(String name, String group, Class<? extends Job> jobClass,
                         boolean durability, boolean recover) {
        setName(name);
        setGroup(group);
        setJobClass(jobClass);
        setDurability(durability);
        setRequestsRecovery(recover);
    }

    /**
     * <p>
     * Get the name of this <code>Job</code>.
     * </p>
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Set the name of this <code>Job</code>.
     * </p>
     *
     * @throws IllegalArgumentException if name is null or empty.
     */
    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Job name cannot be empty.");
        }

        this.name = name;
        this.key = null;
    }

    /**
     * <p>
     * Get the group of this <code>Job</code>.
     * </p>
     */
    public String getGroup() {
        return group;
    }

    /**
     * <p>
     * Set the group of this <code>Job</code>.
     * </p>
     *
     * @param group if <code>null</code>, Scheduler.DEFAULT_GROUP will be used.
     * @throws IllegalArgumentException if the group is an empty string.
     */
    public void setGroup(String group) {
        if (group != null && group.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "Group name cannot be empty.");
        }

        if (group == null) {
            group = Scheduler.DEFAULT_GROUP;
        }

        this.group = group;
        this.key = null;
    }

    /**
     * <p>
     * Returns the 'full name' of the <code>JobDetail</code> in the format
     * "group.name".
     * </p>
     */
    public String getFullName() {
        return group + "." + name;
    }

    @Override
    public JobKey getKey() {
        if (key == null) {
            if (getName() == null) {
                return null;
            }
            key = new JobKey(getName(), getGroup());
        }
        return key;
    }

    public void setKey(JobKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null!");
        }
        setName(key.getName());
        setGroup(key.getGroup());
        this.key = key;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <p>
     * Set a description for the <code>Job</code> instance - may be useful
     * for remembering/displaying the purpose of the job, though the
     * description has no meaning to Quartz.
     * </p>
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    /**
     * <p>
     * Set the instance of <code>Job</code> that will be executed.
     * </p>
     *
     * @throws IllegalArgumentException if jobClass is null or the class is not a <code>Job</code>.
     */
    public void setJobClass(Class<? extends Job> jobClass) {
        if (jobClass == null) {
            throw new IllegalArgumentException("Job class cannot be null.");
        }
        if (!Job.class.isAssignableFrom(jobClass)) {
            throw new IllegalArgumentException(
                    "Job class must implement the Job interface.");
        }
        this.jobClass = jobClass;
    }

    @Override
    public JobDataMap getJobDataMap() {
        if (jobDataMap == null) {
            jobDataMap = new JobDataMap();
        }
        return jobDataMap;
    }

    /**
     * <p>
     * Set the <code>JobDataMap</code> to be associated with the <code>Job</code>.
     * </p>
     */
    public void setJobDataMap(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    /**
     * <p>
     * Set whether or not the <code>Job</code> should remain stored after it
     * is orphaned (no <code>{@link Trigger}s</code> point to it).
     * </p>
     *
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     */
    public void setDurability(boolean durability) {
        this.durability = durability;
    }

    /**
     * <p>
     * Set whether or not the the <code>Scheduler</code> should re-execute
     * the <code>Job</code> if a 'recovery' or 'fail-over' situation is
     * encountered.
     * </p>
     *
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     *
     * @see JobExecutionContext#isRecovering()
     */
    public void setRequestsRecovery(boolean shouldRecover) {
        this.shouldRecover = shouldRecover;
    }

    @Override
    public boolean isDurable() {
        return durability;
    }

    /**
     * @return whether the associated Job class carries the {@link PersistJobDataAfterExecution} annotation.
     */
    @Override
    public boolean isPersistJobDataAfterExecution() {

        return ClassUtils.isAnnotationPresent(jobClass, PersistJobDataAfterExecution.class);
    }

    /**
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     */
    @Override
    public boolean isConcurrentExectionDisallowed() {

        return ClassUtils.isAnnotationPresent(jobClass, DisallowConcurrentExecution.class);
    }

    @Override
    public boolean requestsRecovery() {
        return shouldRecover;
    }

    /**
     * Return a simple string representation of this object.
     */
    @Override
    public String toString() {
        return "JobDetail '" + getFullName() + "':  jobClass: '"
                + ((getJobClass() == null) ? null : getJobClass().getName())
                + " concurrentExectionDisallowed: " + isConcurrentExectionDisallowed()
                + " persistJobDataAfterExecution: " + isPersistJobDataAfterExecution()
                + " isDurable: " + isDurable() + " requestsRecovers: " + requestsRecovery();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobDetail)) {
            return false;
        }
        JobDetail other = (JobDetail) obj;
        if (other.getKey() == null || getKey() == null) {
            return false;
        }
        if (!other.getKey().equals(getKey())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        JobKey key = getKey();
        return key == null ? 0 : getKey().hashCode();
    }

    @Override
    public Object clone() {
        JobDetailImpl copy;
        try {
            copy = (JobDetailImpl) super.clone();
            if (jobDataMap != null) {
                copy.jobDataMap = (JobDataMap) jobDataMap.clone();
            }
        } catch (CloneNotSupportedException ex) {
            throw new IncompatibleClassChangeError("Not Cloneable.");
        }

        return copy;
    }

    @Override
    public JobBuilder getJobBuilder() {
        JobBuilder b = JobBuilder.newJob()
                .ofType(getJobClass())
                .requestRecovery(requestsRecovery())
                .storeDurably(isDurable())
                .usingJobData(getJobDataMap())
                .withDescription(getDescription())
                .withIdentity(getKey());
        return b;
    }
}
