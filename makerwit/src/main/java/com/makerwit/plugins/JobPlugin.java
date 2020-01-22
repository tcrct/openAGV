package com.makerwit.plugins;

import com.makerwit.quartz.AgvJobKit;
import com.makerwit.quartz.QuartzJobModel;
import com.openagv.mvc.core.annnotations.Job;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IPlugin;
import com.openagv.mvc.helpers.ClassHelper;
import com.openagv.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务插件
 *
 * @author Laotang
 */
public class JobPlugin implements IPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(JobPlugin.class);

    private static final List<QuartzJobModel> QUARTZ_JOB_MODEL_LIST = new ArrayList<>();

    public JobPlugin() {

        List<Class<?>> jobClassList = ClassHelper.duang().getJobClassList();
        if (ToolsKit.isEmpty(jobClassList)) {
            throw new AgvException("没有扫描到相关的Job类，请确保已经在添加@Job类注解");
        }

        for (Class<?> jobClass : jobClassList) {
            Job jobAnnon = jobClass.getAnnotation(Job.class);
            if (ToolsKit.isNotEmpty(jobAnnon)) {
                QUARTZ_JOB_MODEL_LIST.add(new QuartzJobModel(jobClass, jobAnnon.cron()));
            }
        }

    }

    @Override
    public boolean start() {
        if (QUARTZ_JOB_MODEL_LIST.isEmpty()) {
            LOG.info("没有扫描到相关的Job类，请确保已经在添加@Job类注解");
            return false;
        }
        AgvJobKit.duang()
                .addJobs(QUARTZ_JOB_MODEL_LIST)
                .run();
        return true;
    }
}
