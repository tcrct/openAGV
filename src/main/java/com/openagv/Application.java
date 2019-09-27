package com.openagv;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.openagv.core.AppContext;
import com.openagv.core.AutoImportModule;
import com.openagv.opentcs.OpenTcsConfigure;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.route.RouteHelper;
import com.openagv.tools.ToolsKit;
import org.opentcs.guing.RunPlantOverview;
import org.opentcs.kernel.RunKernel;
import org.opentcs.kernelcontrolcenter.RunKernelControlCenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hello world!
 *
 */
public class Application {

    private final static Log logger = LogFactory.get();

    private static Application application;

    public static Application duang() {
        if(application == null) {
            application = new Application();
        }
        return application;
    }

    private Application(){
    }

    public Application handlers(List<IHandler> handlers) {
        AppContext.getBeforeHeandlerList().addAll(handlers);
        return this;
    }

    public Application plugins(List<IPlugin> plugins) {
        AppContext.getPluginList().addAll(plugins);
        return this;
    }

    private void guiceInjector() {
        if(null == AppContext.getGuiceInjector()){
            AppContext.getModules().add(new AutoImportModule());
            AppContext.setGuiceInjector(Guice.createInjector(AppContext.getModules()));
        }
    }

    private void startPlugins() {
        try {
            for (Iterator<IPlugin> it = AppContext.getPluginList().iterator(); it.hasNext();) {
                IPlugin plugin = it.next();
                plugin.start();
                Class[] classeArray = plugin.getClass().getInterfaces();
                if(ToolsKit.isNotEmpty(classeArray) && classeArray.length > 1) {
                    System.out.println(classeArray[0]);
                    System.out.println(classeArray[1]);
                }
                logger.warn("插件[{}]启动成功!", plugin.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void configure() {
        OpenTcsConfigure.init();
    }

    private void route() {
        RouteHelper.getRoutes();
    }

    private void startOpenTcs() throws Exception{
        // 启动内核
        RunKernel.main(null);
        logger.warn("启动内核完成");

        // 启动内核心控制中心
        RunKernelControlCenter.main(null);
        logger.warn("启动内核心控制中心完成");

        // 启动工厂概述控制中心
        RunPlantOverview.main(null);
        logger.warn("启动工厂概述控制中心完成");
    }

    public void run() throws Exception {
        // 设置OpenTCS所需要的配置文件
        configure();
        // 启动插件
        startPlugins();
        // 依赖注入
        guiceInjector();
        // 映射路由
        route();
        // 启动OpenTCS
        startOpenTcs();
    }

}
