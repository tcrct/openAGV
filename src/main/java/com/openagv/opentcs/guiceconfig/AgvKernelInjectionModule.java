package com.openagv.opentcs.guiceconfig;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.openagv.opentcs.adapter.CommAdapterFactory;
import com.openagv.opentcs.adapter.ComponentsFactory;
import org.opentcs.customizations.kernel.KernelInjectionModule;

public class AgvKernelInjectionModule extends KernelInjectionModule {

    private static final Log logger = LogFactory.get();

    @Override
    protected void configure() {
        // 安装及绑定通讯工厂
        install(new FactoryModuleBuilder().build(ComponentsFactory.class));
        vehicleCommAdaptersBinder().addBinding().to(CommAdapterFactory.class);
        logger.info("安装及绑定通讯工厂成功");
    }
}