package com.robot.mvc.helpers;

import com.robot.config.Application;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 插件帮助器
 */
public class PluginsHelper {

    private static final Logger LOG = LoggerFactory.getLogger(PluginsHelper.class);

    private final static Lock lock = new ReentrantLock();

    private static PluginsHelper PLUGIN_HELPER = null;

    public static PluginsHelper duang() {
        try {
            lock.lock();
            if (null == PLUGIN_HELPER) {
                PLUGIN_HELPER = new PluginsHelper();
            }
        } finally {
            lock.unlock();
        }
        return PLUGIN_HELPER;
    }

    private PluginsHelper() {
        try {
            for (Iterator<IPlugin> it = Application.PLUGIN_LIST.iterator(); it.hasNext(); ) {
                IPlugin plugin = it.next();
                plugin.start();
                LOG.warn("插件[{" + plugin.getClass().getName() + "}]启动成功!");
            }
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }


}
