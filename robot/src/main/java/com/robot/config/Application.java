package com.robot.config;

import cn.hutool.core.util.ClassUtil;
import com.robot.RobotContext;
import com.robot.hotswap.HotSwapWatcher;
import com.robot.mvc.core.interfaces.IComponents;
import com.robot.mvc.core.interfaces.IHandler;
import com.robot.mvc.core.interfaces.IPlugin;
import com.robot.mvc.core.interfaces.ISystemInit;
import com.robot.mvc.helpers.ClassHelper;
import com.robot.mvc.helpers.IocHelper;
import com.robot.mvc.helpers.PluginsHelper;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.utils.RobotUtil;
import org.opentcs.guing.RunPlantOverview;
import org.opentcs.kernel.RunKernel;
import org.opentcs.kernelcontrolcenter.RunKernelControlCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序启动Robot
 *
 * @author Laotang
 * @date 2020/2/2.
 * @since 1.0
 */
public class Application {

    private final static Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * 是否正常启动完成， 正常启动完成大于0
     */
    private long startedTime = 0L;
    /**
     * 需要启热启动的目录
     */
    private String hotSwapDir;
    /**
     * 在执行Controller前的处理器链
     */
    public static List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    /**
     * 在执行Controller后的处理器链
     */
    public static List<IHandler> AFTER_HEANDLER_LIST = new ArrayList<>();
    /**
     * 插件
     */
    public static final List<IPlugin> PLUGIN_LIST = new ArrayList<>();

    private ISystemInit systemInit;
    private static Application application;

    public static Application duang() {
        if (application == null) {
            application = new Application();
        }
        return application;
    }

    public Application beforeHandlers(List<IHandler> handlers) {
        BEFORE_HEANDLER_LIST.addAll(handlers);
        return this;
    }

    public Application afterHandlers(List<IHandler> handlers) {
        AFTER_HEANDLER_LIST.addAll(handlers);
        return this;
    }

    public Application plugins(List<IPlugin> plugins) {
        PLUGIN_LIST.addAll(plugins);
        return this;
    }

    public Application components(IComponents components) {
        RobotContext.setRobotComponents(components);
        return this;
    }

    public Application init(ISystemInit systemInit) {
        this.systemInit = systemInit;
        return this;
    }

    public Application hotSwap() {
        this.hotSwapDir = ClassUtil.getClassPath();
        return this;
    }

    public boolean isStarted() {
        return startedTime > 0L;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(long startedTime) {
        this.startedTime = startedTime;
    }

    public void run() {
        try {
            // 扫描类
            ClassHelper.duang();
            // 启动插件
            PluginsHelper.duang();
            // 映射路由
            RouteHelper.duang();
            // 依赖注入
            IocHelper.duang();
            // 启动OpenTCS
            startOpenTcs();
            // 初始化
            initRobot();
            // 是否启动完成
            startedTime = System.currentTimeMillis();
        } catch (Exception e) {
            startedTime = 0L;
            LOG.error("启动时发生异常: {}，程序退出！{}", e.getMessage(), e);
            System.exit(1);
        }
        // 热部署
         hotSwapWatcher();
    }

    private void startOpenTcs() throws Exception {

        //设置环境变量
        OpenAgvConfigure.duang();

        // 启动内核
        RunKernel.main(null);
        LOG.warn("启动内核完成");

        // 启动内核心控制中心
        RunKernelControlCenter.main(null);
        LOG.warn("启动内核心控制中心完成");


        // 启动工厂概述控制中心
        RunPlantOverview.main(null);
        LOG.warn("启动工厂概述控制中心完成");

    }

    private void initRobot() throws Exception {
        systemInit.init();
    }

    /**
     * 开发模式下开启热部署功能，必须要开启IDEA的自动编译功能
     * File-->Setting-->Build-->Compiler--> Build project automatically
     * ctrl+alt+shift+/ -- > Registry...  --> compiler.automake.allow when.app.runing (勾选中后退出）
     *
     * 在IDEA下，需要按下ctrl+s组合键进行保存，以达到快速启动热部署功能
     */
    private void hotSwapWatcher() {
        if(RobotUtil.isDevMode() || !hotSwapDir.isEmpty()) {
            HotSwapWatcher watcher  = new HotSwapWatcher(hotSwapDir);
            watcher.run();
        }
    }
}
