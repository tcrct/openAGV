package com.makerwit.quartz;

import cn.hutool.core.util.IdUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.utils.ToolsKit;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

/**
 * 定时器
 * @author Laotang
 */
public class AgvJobKit {

    private static Log LOG = LogFactory.get();

    private static final String JOB_GROUP_FIELD = "AGV_JOB";
    private static final String JOB_FIELD = "Job";
    private static final String TRIGGER_FIELD = "Trigger";
    private Scheduler scheduler;

    private static class AgvJobHandlerHolder {
        private static final AgvJobKit INSTANCE = new AgvJobKit();
    }
    private AgvJobKit() {
        init();
    }
    public static final AgvJobKit duang() {
        return AgvJobKit.AgvJobHandlerHolder.INSTANCE;
    }

    private void init() {
        try {
            StdSchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AgvJobKit addJobs(List<QuartzJobModel> quartzJobList) {
        for (QuartzJobModel job : quartzJobList) {
            addJob(job.getJobClass(), job.getCronExpression());
        }
        return this;
    }

    /**
     * @param jobClass
     * @param cronExpression cron表达式
     * @return
     */
    public AgvJobKit addJob(Class jobClass, String cronExpression) {

        if (ToolsKit.isEmpty(scheduler)) {
            throw new RobotException("创建定时任器调度器时，scheduler为空");
        }

        boolean isExtendsJob = false;
        Class[] interfaceArray = jobClass.getInterfaces();
        for (Class interfaceClass : interfaceArray) {
            if (Job.class.equals(interfaceClass)) {
                isExtendsJob = true;
                break;
            }
        }
        if (!isExtendsJob) {
            throw new RobotException(jobClass.getName() + "必须要实现" + Job.class.getName() + "接口");
        }
        if (ToolsKit.isEmpty(cronExpression)) {
            throw new RobotException("定时器cron表达式不能为空");
        }
       // 构建标识
        String key = IdUtil.objectId();
        // 构建一个作业实例
        String jobDetailKey = getJobKey(key);
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobDetailKey, JOB_GROUP_FIELD).build();
        // 构建一个触发器，规定触发的规则
        String triggerKey = getTriggerKey(key);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey, JOB_GROUP_FIELD)
                .forJob(jobDetail)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("Quatrz 添加任务{}成功", jobDetail.getKey());
        } catch (Exception e) {
            throw new RobotException("添加定时任务时出错: " + e.getMessage(), e);
        }
        return this;
    }

    private String getJobKey(String key) {
        return JOB_FIELD+"_"+ key;
    }
    private String getTriggerKey(String key) {
        return TRIGGER_FIELD + "_" + key;
    }

    public void run() {
        if (ToolsKit.isEmpty(scheduler)) {
            throw new RobotException("创建定时任器调度器时，scheduler为空");
        }
        try {
            scheduler.start();
        }  catch (Exception e) {
            throw new RobotException("定时任务器启动失败: " + e.getMessage(), e);
        }

    }

}
