package com.openagv.adapter;

import com.openagv.contrib.netty.comm.IChannelManager;
import com.openagv.contrib.netty.comm.NetChannelType;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.utils.AgvKit;
import com.openagv.mvc.utils.SettingUtils;

/**
 * 车辆网络管理器
 * Created by laotang on 2020/1/25.
 */
public class VehicleChannelManager {

    public static IChannelManager getChannelManager(AgvCommAdapter adapter) {
        // 如果是以服务器方式启动，则在初始化时完成
        String runType = SettingUtils.getString("run.type", "server");
        NetChannelType channelType = AgvKit.getNetChannelType();
        if ("server".equalsIgnoreCase(runType)) {
            return getServerChannelManager(channelType);
        } else if ("client".equalsIgnoreCase(runType)) {
            return getClientChannelManager(channelType, adapter.getName());
        } else {
            throw new AgvException("配置文件[run.type]参数值设置不正确，仅允许server/client两个选项");
        }
    }

    private static IChannelManager getServerChannelManager(NetChannelType channelType) {
        String host = AgvKit.getServerHost();
        int port = AgvKit.getServerPort();
        if (NetChannelType.TCP.equals(channelType)) {

        } else if (NetChannelType.UDP.equals(channelType)) {

        } else if (NetChannelType.RXTX.equals(channelType)) {

        }
        return null;
    }

    private static IChannelManager getClientChannelManager(NetChannelType channelType, String vheicleName) {
        String host = AgvKit.getHost(vheicleName);
        int port = AgvKit.getPort(vheicleName);
        if (NetChannelType.TCP.equals(channelType)) {

        } else if (NetChannelType.UDP.equals(channelType)) {

        } else if (NetChannelType.RXTX.equals(channelType)) {

        }
        return null;
    }
}
