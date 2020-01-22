package com.makerwit.quartz;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.makerwit.core.component.RepeatSend;
import com.openagv.AgvContext;
import com.openagv.mvc.core.annnotations.Job;
import com.openagv.mvc.core.interfaces.IRepeatSend;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 重发报文任务
 *每秒执行一次任务
 *
 * @author Laotang
 */
@Job(cron="0/1 * * * * ?")
public class RepeatSendJob implements org.quartz.Job {

    private static final Log LOG = LogFactory.get();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        RepeatSend.duang().sendAll(null);
        LOG.info("RepeatSendJob execute:  {}", context);
    }
}
