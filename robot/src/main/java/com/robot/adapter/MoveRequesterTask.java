package com.robot.adapter;

import com.google.inject.assistedinject.Assisted;
import com.robot.mvc.utils.SettingUtils;
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
    private int requestInterval = 2000;

    /**
     * 构造方法
     *
     * @param actionListener The actual action to be performed to enqueue requests.
     */
    @Inject
    public MoveRequesterTask(@Nonnull @Assisted ActionListener actionListener) {
        this.actionListener = requireNonNull(actionListener, "actionListener is null");
        setRequestInterval(SettingUtils.getInt("handshake.interval", "adapter", requestInterval));
    }

    public void enable() {
        if (moveRequestTimer != null) {
            return;
        }
        LOG.info("启动定时执行移动命令任务");
        moveRequestTimer = new Timer(requestInterval, actionListener);
        moveRequestTimer.start();
    }

    public void disable() {
        if (moveRequestTimer == null) {
            return;
        }
        LOG.info("停止定时执行移动命令任务");
        moveRequestTimer.stop();
        moveRequestTimer = null;
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
