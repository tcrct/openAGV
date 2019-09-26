package com.openagv.opentcs.guiceconfig;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.openagv.opentcs.adapter.CommAdapterPanelFactory;
import com.openagv.opentcs.adapter.PanelComponentsFactory;
import org.opentcs.customizations.controlcenter.ControlCenterInjectionModule;

/**
 *
 */
public class AgvControlCenterInjectionModule extends ControlCenterInjectionModule {

    @Override
    protected void configure() {

        install(new FactoryModuleBuilder().build(PanelComponentsFactory.class));

        commAdapterPanelFactoryBinder().addBinding().to(CommAdapterPanelFactory.class);


//        install(new FactoryModuleBuilder().build(AdapterPanelComponentsFactory.class));
//        commAdapterPanelFactoryBinder().addBinding().to(LoopbackCommAdapterPanelFactory.class);
    }

}
