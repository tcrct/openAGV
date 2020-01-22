package com.openagv.mvc.utils;

import com.openagv.AgvContext;
import com.openagv.contrib.netty.comm.NetChannelType;

/**
 * Created by laotang on 2020/1/22.
 */
public class AgvKit {

    public static NetChannelType getNetChannelType() {
        NetChannelType type = AgvContext.getNetChannelType();
        if (ToolsKit.isEmpty(type)) {
            type = NetChannelType.UDP;
        }
        return type;
    }

    public static String getHost(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtils.getString("host", "net", "0.0.0.0");
//            return  AgvContext.getAdapter(vehicleName).getProcessModel().getVehicleHost();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getString("name", "serialport", "COM6");
        }
        return "";
    }

    public static Integer getPort(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtils.getInt("port", "net", 9090);
//            return AgvContext.getAdapter(vehicleName).getProcessModel().getVehiclePort();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getInt("baudrate","serialport", 38400);
        }
        return 0;
    }
}
