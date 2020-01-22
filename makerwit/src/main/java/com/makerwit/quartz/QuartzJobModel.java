package com.makerwit.quartz;

/**
 * 定时任务对象
 *
 * Created by laotang on 2020/1/22.
 */
public class QuartzJobModel {

    // 任务类
    private Class jobClass;
     // cron表达式
    private String cronExpression;

    public QuartzJobModel(Class jobClass, String cronExpression) {
        this.jobClass = jobClass;
        this.cronExpression = cronExpression;
    }

    public Class getJobClass() {
        return jobClass;
    }

    public String getCronExpression() {
        return cronExpression;
    }

}
