package com.openagv.opentcs.adapter;

import com.openagv.core.AppContext;
import com.openagv.opentcs.enums.CommunicationType;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/**
 * 适配器工厂
 *
 * @author Laotang
 */
public class CommAdapterFactory implements VehicleCommAdapterFactory {

    private static final Logger logger = Logger.getLogger(CommAdapterFactory.class);

    private ComponentsFactory componentsFactory;

    private boolean initialized;

    @Inject
    public CommAdapterFactory(ComponentsFactory componentsFactory) {
        this.componentsFactory = requireNonNull(componentsFactory, "组件工厂对象不能为空");
    }

    @Override
    public void initialize() {
        if(initialized) {
            logger.warn("适配器重复初始化");
            return;
        }
        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if(!initialized) {
            logger.warn("适配器没有初始化");
            return;
        }
        initialized = false;
    }

    /**
     * 创智通讯适配器的名称
     * @return 名称
     */
    @Override
    public String getAdapterDescription() {
        return getDescription().getDescription();
    }

    @Override
    public CommAdapterDescription getDescription() {
        return new CommAdapterDescription();
    }

    @Override
    public boolean providesAdapterFor(Vehicle vehicle) {
        java.util.Objects.requireNonNull(vehicle,"vehicle is null");
        // 如果不是串口的方式，必须要先设置车辆的Host及Port
        if(!CommunicationType.SERIALPORT.equals(AppContext.getCommunicationType())) {
            if (ToolsKit.isEmpty(ToolsKit.getVehicleHostName())) {
                logger.error("车辆没有设置Host，请先设置");
                return false;
            }
            if (ToolsKit.isEmpty(ToolsKit.getVehiclePortName())) {
                logger.error("车辆没有设置Port，请先设置");
                return false;
            }
            try {
                // 端口设置范围
                int port = Integer.parseInt(vehicle.getProperty(ToolsKit.getVehiclePortName()));
                checkInRange(port, ToolsKit.getMinPort(), ToolsKit.getMaxPort(), "port value");
            } catch (IllegalArgumentException e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public VehicleCommAdapter getAdapterFor(@Nonnull Vehicle vehicle) {
        java.util.Objects.requireNonNull(vehicle,"车辆不能为空");
        // 验证一下端口
        if(!providesAdapterFor(vehicle)){
            return null;
        }
        CommAdapter commAdapter = componentsFactory.createCommAdapter(vehicle);
        requireNonNull(commAdapter, "通讯适配器对象不能为空，请检查！");
        commAdapter.getProcessModel().setVehicleHost(vehicle.getProperty(ToolsKit.getVehicleHostName()));
        commAdapter.getProcessModel().setVehiclePort(Integer.parseInt(vehicle.getProperty(ToolsKit.getVehiclePortName())));
        return commAdapter;
    }

}
