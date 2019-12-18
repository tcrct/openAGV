package com.openagv.opentcs.adapter;

import com.openagv.core.AppContext;
import com.openagv.opentcs.model.VehicleModelTO;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanelFactory;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * 面板适配器工厂
 *
 * @author Laotang
 */
public class CommAdapterPanelFactory implements VehicleCommAdapterPanelFactory {

    private final static Logger logger = LoggerFactory.getLogger(CommAdapterPanelFactory.class);

    /** 服务门户*/
    private final KernelServicePortal servicePortal;
    /**组件工厂*/
    private final PanelComponentsFactory componentsFactory;
    /**是否已初始化此工厂，true为已初始化*/
    private boolean initialized;


    @Inject
    public CommAdapterPanelFactory(KernelServicePortal servicePortal,
                                   PanelComponentsFactory componentsFactory,@ApplicationEventBus EventSource eventSource) {
        this.servicePortal = requireNonNull(servicePortal, "servicePortal");
        AppContext.setKernelServicePortal(servicePortal);
        AppContext.setEventSource(eventSource);
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    }

    @Override
    public void initialize() {
        if(isInitialized()) {
            return;
        }
        this.initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if(!isInitialized()){
            return;
        }
        this.initialized = false;
    }

    @Override
    public List<VehicleCommAdapterPanel> getPanelsFor(
            @Nonnull VehicleCommAdapterDescription description,
            @Nonnull TCSObjectReference<Vehicle> vehicle,
            @Nonnull VehicleProcessModelTO processModel) {

        requireNonNull(description, "description");
        requireNonNull(vehicle, "vehicle");
        requireNonNull(processModel, "processModel");

        /**
         * 必须先实现CommAdapter里的createCustomTransferableProcessModel方法，否则(processModel instanceof VehicleModelTO)会返回false
         */
       if(!providesPanelsFor(description, processModel)) {
            logger.warn("无法为具有'{}'适配器的'{}'提供面板.", description.getDescription(), processModel.getVehicleName());
           return new ArrayList<>();
        }

        List<VehicleCommAdapterPanel> panels = new ArrayList<>();
        VehicleModelTO vehicleModelTO= (VehicleModelTO)processModel;
        panels.add(componentsFactory.createControlPanel(vehicleModelTO, servicePortal.getVehicleService()));

        return panels;
    }


    private boolean providesPanelsFor(VehicleCommAdapterDescription description,
                                      VehicleProcessModelTO processModel) {
        return (description instanceof CommAdapterDescription)
                && (processModel instanceof VehicleModelTO);
    }

}
