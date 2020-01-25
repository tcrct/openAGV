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

    /***
     * 以服务器方式启动时的地址
     * @return
     */
    public static String getServerHost() {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtils.getString("host", "net", "0.0.0.0");
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getString("name", "serialport", "COM6");
        }
        return "";
    }

    /***
     * 以服务器方式启动时的端口
     * @return
     */
    public static Integer getServerPort() {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return SettingUtils.getInt("port", "net", 9090);
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getInt("baudrate","serialport", 38400);
        }
        return 0;
    }

    /***
     * 以客户端方式启动时的地址
     * @param vehicleName 车辆名称
     * @return
     */
    public static String getHost(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return  AgvContext.getAdapter(vehicleName).getProcessModel().getVehicleHost();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getString("name", "serialport", "COM6");
        }
        return "";
    }

    /***
     * 以客户端方式启动时的端口
     * @param vehicleName 车辆名称
     * @return
     */
    public static Integer getPort(String vehicleName) {
        if (NetChannelType.TCP.equals(getNetChannelType()) ||
                NetChannelType.UDP.equals(getNetChannelType())) {
            return AgvContext.getAdapter(vehicleName).getProcessModel().getVehiclePort();
        } else if (NetChannelType.RXTX.equals(getNetChannelType())) {
            return SettingUtils.getInt("baudrate","serialport", 38400);
        }
        return 0;
    }
}
