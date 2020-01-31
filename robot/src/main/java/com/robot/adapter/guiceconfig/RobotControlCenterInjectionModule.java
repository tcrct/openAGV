package com.robot.adapter.guiceconfig;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.robot.adapter.exchange.RobotCommAdapterPanel;
import com.robot.adapter.exchange.RobotCommAdapterPanelFactory;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 * A custom Guice module for project-specific configuration.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class RobotControlCenterInjectionModule
        extends ControlCenterInjectionModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(RobotCommAdapterPanel.class));

        commAdapterPanelFactoryBinder().addBinding().to(RobotCommAdapterPanelFactory.class);
    }
}
