package com.robot.adapter;

import com.google.inject.assistedinject.Assisted;
import com.robot.mvc.utils.SettingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

/**
 * 移动请求任务定时器
 *
 * @author Laotang
 */
public class MoveRequesterTask {

    private static final Logger LOG = LoggerFactory.getLogger(MoveRequesterTask.class);

    /**动作监听器*/
    private final ActionListener actionListener;
    /**执行移动命令的计时器*/
    private Timer moveRequestTimer;
    /**执行时间间隔，毫秒作单位*/
    private int requestInterval = 1000;

    /**
     * 构造方法
     *
     * @param actionListener The actual action to be performed to enqueue requests.
     */
    @Inject
    public MoveRequesterTask(@Nonnull @Assisted ActionListener actionListener) {
        this.actionListener = requireNonNull(actionListener, "actionListener is null");
        setRequestInterval(SettingUtil.getInt("move.interval", "adapter", requestInterval));
    }

    /**
     * 开启车辆通讯适配器移动指令定时任务
     *
     * @param vehicleName 车辆名称
     */
    public void enable(String vehicleName) {
        if (moveRequestTimer != null) {
            return;
        }
        moveRequestTimer = new Timer(requestInterval, actionListener);
        moveRequestTimer.start();
        LOG.info("启动车辆通讯适配器[{}]的移动指令定时任务[actionListener.hashCode: {}]成功！", vehicleName, actionListener.hashCode());
    }

    /**
     *停止车辆通讯适配器移动指令定时任务
     * @param vehicleName 车辆名称
     */
    public void disable(String vehicleName) {
        if (moveRequestTimer == null) {
            return;
        }
        moveRequestTimer.stop();
        moveRequestTimer = null;
        LOG.info("停止车辆通讯适配器[{}]的移动指令定时任务[{}]成功！", vehicleName, actionListener.hashCode());
    }

    /**
     * 重启
     */
    public void restart() {
        if (moveRequestTimer == null) {
            LOG.debug("Not enabled, doing nothing.");
            return;
        }
        moveRequestTimer.restart();
    }

    /**
     * 设置任务队列的间隔时间
     *
     * @param requestInterval  间隔时间值，毫秒作单位
     */
    public void setRequestInterval(int requestInterval) {
        this.requestInterval = requestInterval;
    }
}
