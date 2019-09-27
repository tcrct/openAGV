package com.openagv.core;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.setting.Setting;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.openagv.core.annotations.Controller;
import com.openagv.core.annotations.Service;
import com.openagv.route.RouteHelper;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;

import java.util.Set;

/**
 * 自动注入对象，基于google guice
 * 每个Service必须要实现一个接口且注明@Service注解
 *
 * @author Laotang
 */
public class AutoImportModule extends AbstractModule {

    private final static Log logger = LogFactory.get();

    @Override
    public void configure() {
        String packageName = SettingUtils.getString("package.name");
        java.util.Objects.requireNonNull(packageName, "请在app.setting文件里先设置package.name");
        Set<Class<?>> classSet = ClassUtil.scanPackage(packageName, new Filter<Class<?>>() {
            @Override
            public boolean accept(Class<?> aClass) {
                return ToolsKit.isInjectClass(aClass);
            }
        });
        for(Class clazz : classSet) {
            binder(clazz);
        }
        AppContext.getInjectClassSet().addAll(classSet);
    }

    /**
     * 绑定到容器
     * @param clazz 要注入的类
     * @param <T>  泛型类
     */
    private <T> void binder(Class<T> clazz) {
        java.util.Objects.requireNonNull(clazz,"要注入的class不能为null");
        try {
            if(ToolsKit.isInjectServiceClass(clazz)) {
                Class<T> serviceInterfaceClass = (Class<T>) clazz.getInterfaces()[0];
                bind(serviceInterfaceClass).to(clazz).in(Scopes.SINGLETON);
            }
        } catch (Exception e){
            logger.error("bind is fail : " + e.getMessage(), e);
        }
    }



}
