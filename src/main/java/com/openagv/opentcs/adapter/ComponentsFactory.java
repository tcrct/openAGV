package com.openagv.opentcs.adapter;

import com.openagv.opentcs.telegrams.StateRequesterTask;
import org.opentcs.data.model.Vehicle;

import java.awt.event.ActionListener;


/**
 * 通信适配器的各种实例的工厂
 *
 * @author Laotang
 */
public interface ComponentsFactory {

    /**
     * 创建一个新的通讯适配器(CommAdapter)给车辆
     *
     * @param vehicle 车辆
     * @return CommAdapter
     */
    CommAdapter createCommAdapter(Vehicle vehicle);
}
