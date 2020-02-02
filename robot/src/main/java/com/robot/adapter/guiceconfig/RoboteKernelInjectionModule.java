package com.robot.adapter.guiceconfig;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.robot.adapter.exchange.AdapterComponentsFactory;
import com.robot.adapter.RobotCommAdapterFactory;
import org.opentcs.customizations.kernel.KernelInjectionModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoboteKernelInjectionModule
        extends KernelInjectionModule {

    private static final Logger LOG = LoggerFactory.getLogger(RoboteKernelInjectionModule.class);

    @Override
    protected void configure() {

//        RobotCommAdapterConfiguration configuration
//                = getConfigBindingProvider().get(RobotCommAdapterConfiguration.PREFIX,
//                RobotCommAdapterConfiguration.class);
//
//        if (!configuration.enable()) {
//            LOG.error("######## 配置文件里没有开启Robot适配器 ########");
//            return;
//        }

        install(new FactoryModuleBuilder().build(AdapterComponentsFactory.class));
        vehicleCommAdaptersBinder().addBinding().to(RobotCommAdapterFactory.class);
    }
}
