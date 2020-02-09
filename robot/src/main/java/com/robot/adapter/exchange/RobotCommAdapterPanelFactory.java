package com.robot.adapter.exchange;

import com.robot.RobotContext;
import com.robot.adapter.model.RobotVehicleModelTO;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommAdapterPanelFactory;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
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
public class RobotCommAdapterPanelFactory implements VehicleCommAdapterPanelFactory {

    private final static Logger logger = LoggerFactory.getLogger(RobotCommAdapterPanelFactory.class);

    /**
     * 服务门户
     */
    private final KernelServicePortal servicePortal;
    /**
     * 组件工厂
     */
    private final RobotPanelComponentsFactory componentsFactory;
    /**
     * 是否已初始化此工厂，true为已初始化
     */
    private boolean initialized;


    @Inject
    public RobotCommAdapterPanelFactory(KernelServicePortal servicePortal, RobotPanelComponentsFactory componentsFactory) {
        this.servicePortal = requireNonNull(servicePortal, "servicePortal");
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        RobotContext.setKernelServicePortal(servicePortal);
    }

    @Override
    public void initialize() {
        if (isInitialized()) {
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
        if (!isInitialized()) {
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
         * 必须先实现Adapter里的createCustomTransferableProcessModel方法，否则(processModel instanceof RobotVehicleModelTO)会返回false
         */
        if (!providesPanelsFor(description, processModel)) {
            logger.warn("不能为车辆'{}'提供'{}'适配器面板.", processModel.getVehicleName(), description.getDescription());
            return new ArrayList<>();
        }

        List<VehicleCommAdapterPanel> panels = new ArrayList<>();
        panels.add(componentsFactory.createControlPanel((RobotVehicleModelTO) processModel, servicePortal.getVehicleService()));

        return panels;
    }


    private boolean providesPanelsFor(VehicleCommAdapterDescription description,
                                      VehicleProcessModelTO processModel) {
        return (description instanceof RobotAdapterDescription)
                && (processModel instanceof RobotVehicleModelTO);
    }

}