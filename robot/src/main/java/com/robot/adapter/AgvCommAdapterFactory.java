/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.adapter;

import com.robot.AgvContext;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.mvc.core.exceptions.AgvException;
import com.robot.mvc.utils.ToolsKit;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

/***
 * OpenAgv通讯适配器工厂
 */
public class AgvCommAdapterFactory implements VehicleCommAdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AgvCommAdapterFactory.class);

    private static final String VEHICLE_HOST = "host";
    private static final String VEHICLE_PORT = "prot";
    /**
     * 适配器组件工厂
     */
    private final AdapterComponentsFactory componentsFactory;
    /**
     * 组件是否已经初始化
     */
    private boolean initialized;

    /**
     * 创建组件工厂
     *
     * @param componentsFactory 创建特定于通信适配器的组件工厂
     */
    @Inject
    public AgvCommAdapterFactory(AdapterComponentsFactory componentsFactory) {
        this.componentsFactory = requireNonNull(componentsFactory, "组件工厂对象不能为空");
    }

    @Override
    public void initialize() {
        if (initialized) {
            LOG.info("OpenAgv适配器工厂重复初始化");
            return;
        }
        initialized = true;
        LOG.info("OpenAgv适配器工厂初始化完成");
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void terminate() {
        if (!initialized) {
            LOG.warn("OpenAgv适配器工厂没有初始化");
            return;
        }
        initialized = false;
        LOG.info("OpenAgv适配器工厂终止");
    }

    /**
     * 通讯适配器名称
     *
     * @return
     */
    @Override
    public VehicleCommAdapterDescription getDescription() {
        return new AdapterDescription();
    }

    @Override
    @Deprecated
    public String getAdapterDescription() {
        return getDescription().getDescription();
    }

    @Override
    public boolean providesAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "车辆不能为空");

        if (NetChannelType.TCP.equals(AgvContext.getNetChannelType()) ||
                NetChannelType.UDP.equals(AgvContext.getNetChannelType())) {
            if (ToolsKit.isEmpty(vehicle.getProperty(VEHICLE_HOST))) {
                throw new AgvException("车辆host没有设置");
            }

            if (ToolsKit.isEmpty(vehicle.getProperty(VEHICLE_PORT))) {
                throw new AgvException("车辆port没有设置");
            }
            try {
                //设置端口范围
                checkInRange(Integer.parseInt(vehicle.getProperty(VEHICLE_PORT)),
                        1024,
                        65535);
            } catch (IllegalArgumentException exc) {
                throw new AgvException("端口范围值须在"+1024+"~"+65535+"之间");
            }
        }

        return true;
    }

    @Override
    public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
        requireNonNull(vehicle, "车辆不能为空");
        try {
            if (!providesAdapterFor(vehicle)) {
                LOG.error("TCP/UDP通讯模式下，车辆必须要设置链接地址及端口");
                return null;
            }

            AgvCommAdapter adapter = componentsFactory.createCommAdapter(vehicle);
            AgvProcessModel processModel = adapter.getProcessModel();
            processModel.setVehicleHost(vehicle.getProperty(VEHICLE_HOST));
            processModel.setVehiclePort(Integer.parseInt(vehicle.getProperty(VEHICLE_PORT)));
            // 加入到缓存集合
            AgvContext.getAdapterMap().put(processModel.getName(), adapter);
            return adapter;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
