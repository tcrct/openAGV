package com.robot.hotswap;

import com.robot.mvc.helpers.BeanHelper;
import com.robot.mvc.helpers.ClassHelper;
import com.robot.mvc.helpers.IocHelper;
import com.robot.mvc.helpers.RouteHelper;
import com.robot.utils.RobotUtil;
import org.opentcs.kernel.extensions.servicewebapi.console.ControllerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by laotang on 2019/5/27.
 */
public class ClassLoaderHelper {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderHelper.class);

    private boolean isDev;
    private DuangClassLoader duangClassLoader;

    public static ClassLoaderHelper getInstance() {
        return new ClassLoaderHelper();
    }

    private ClassLoaderHelper() {
        init();
    }

    private void init() {
        isDev = RobotUtil.isDevMode();
        if(!isDev) {
            logger.info("热部署功能只允许在开发环境下运行");
            return;
        }
        duangClassLoader = new DuangClassLoader();
        Set<String> classKeySet = duangClassLoader.getClassKeySet();
        List<Class<?>> classList = new ArrayList<>(classKeySet.size());
        try {
            for (String classKey : classKeySet) {
                classList.add(duangClassLoader.loadClass(classKey));
            }
            ClassHelper.duang().reSetAllBizClass(classList);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void hotSwap() {
        init();
        try {
            // 清除
            ControllerFactory.getMethodMap().clear();
            // 清除旧的IocBeanMap
            BeanHelper.duang().getIocBeanMap().clear();
            // 重置Class
            RouteHelper.duang().reset();
            // 重新IOC
            IocHelper.duang().ioc();
            logger.warn("hotswap is success");
        } catch (Exception e) {
            logger.warn("hotswap is fail: " + e.getMessage(),e);
        }
    }

}
