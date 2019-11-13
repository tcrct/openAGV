package com.openagv;


import cn.hutool.core.thread.ThreadUtil;
import com.google.inject.Guice;
import com.openagv.core.AppContext;
import com.openagv.core.AutoImportModule;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IAction;
import com.openagv.enums.EnvEnum;
import com.openagv.ioc.IocHelper;
import com.openagv.opentcs.OpenAgvConfigure;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.route.RouteHelper;
import com.openagv.tools.ToolsKit;
import org.opentcs.guing.RunPlantOverview;
import org.opentcs.kernel.RunKernel;
import org.opentcs.kernelcontrolcenter.RunKernelControlCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Hello world!
 *
 */
public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    private static Application application;

    public static Application duang() {
        if(application == null) {
            application = new Application();
        }
        return application;
    }

    private Application(){
    }

    public Application beforeHandlers(List<IHandler> handlers) {
        AppContext.getBeforeHeandlerList().addAll(handlers);
        return this;
    }
    public Application afterHandlers(List<IHandler> handlers) {
        AppContext.getAfterHeandlerList().addAll(handlers);
        return this;
    }

    public Application plugins(List<IPlugin> plugins) {
        AppContext.getPluginList().addAll(plugins);
        return this;
    }

    public Application actions(List<IAction> actions) {
        for(IAction action : actions) {
            AppContext.getCustomActionsQueue().put(action.actionKey(), action);
        }
        return this;
    }

    public Application env(EnvEnum envEnum) {
        AppContext.setEnvModel(envEnum);
        return this;
    }

    private void guiceInjector() throws Exception {
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
                    for(Class clazz : classeArray) {
                        if(IEnable.class.equals(clazz)) {
                            AppContext.getPluginEnableList().add((IEnable) plugin);
                        }
                    }
                }
                logger.warn("插件[{"+plugin.getClass().getName()+"}]启动成功!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Application configure(OpenAgvConfigure configure) {
        AppContext.setAgvConfigure(configure);
        return this;
    }

    private void route() {
        RouteHelper.getRoutes();
    }

    private void injectDao() throws Exception{
        IocHelper.ioc();
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
        java.util.Objects.requireNonNull(AppContext.getAgvConfigure(), "配置文件为空，请先进行设置再启动系统！");
        // 启动插件
        startPlugins();
        // 依赖注入
        guiceInjector();
        // 映射路由
        route();
        // 注入Dao
        injectDao();
        // 启动OpenTCS
        startOpenTcs();
    }

}
