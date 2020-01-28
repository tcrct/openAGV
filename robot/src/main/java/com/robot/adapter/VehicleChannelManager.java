package com.robot.adapter;

import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.contrib.netty.rxtx.RxtxServerManager;
import com.robot.contrib.netty.tcp.TcpClientManager;
import com.robot.contrib.netty.tcp.TcpServerManager;
import com.robot.contrib.netty.udp.UdpClientManager;
import com.robot.contrib.netty.udp.UdpServerManager;
import com.robot.mvc.core.exceptions.AgvException;
import com.robot.mvc.utils.AgvKit;
import com.robot.mvc.utils.SettingUtils;

/**
 * 车辆网络管理器
 * Created by laotang on 2020/1/25.
 */
public class VehicleChannelManager {

    /**
     * 取通讯网络管理器
     * @param adapter
     * @return
     */
    public static IChannelManager getChannelManager(AgvCommAdapter adapter) {
        // 如果是以服务器方式启动，则在初始化时完成
        String runType = SettingUtils.getString("run.type", "server");
        NetChannelType channelType = AgvKit.getNetChannelType();
        if ("server".equalsIgnoreCase(runType)) {
            return getServerChannelManager(adapter, channelType);
        } else if ("client".equalsIgnoreCase(runType)) {
            return getClientChannelManager(adapter, channelType);
        } else {
            throw new AgvException("配置文件[run.type]参数值设置不正确，仅允许server/client两个选项");
        }
    }

    /***
     * 以服务器方式运行的管理器
     * @param adapter 车辆通讯适配器
     * @param channelType  网络渠道类型
     * @return
     */
    private static IChannelManager getServerChannelManager(AgvCommAdapter adapter, NetChannelType channelType) {
        String host = AgvKit.getServerHost();
        int port = AgvKit.getServerPort();
        if (NetChannelType.TCP.equals(channelType)) {
            return new TcpServerManager(adapter);
        } else if (NetChannelType.UDP.equals(channelType)) {
            return new UdpServerManager(adapter);
        } else if (NetChannelType.RXTX.equals(channelType)) {
            return new RxtxServerManager(adapter);
        }
        return null;
    }

    /**
     * 以客户端方式运行的管理器
     * @param adapter 车辆通讯适配器
     * @param channelType   网络渠道类型
     * @return
     */
    private static IChannelManager getClientChannelManager(AgvCommAdapter adapter, NetChannelType channelType) {
        if (NetChannelType.TCP.equals(channelType)) {
            return new TcpClientManager(adapter);
        } else if (NetChannelType.UDP.equals(channelType)) {
            return new UdpClientManager(adapter);
        } else if (NetChannelType.RXTX.equals(channelType)) {
            return new RxtxServerManager(adapter);
        }
        return null;
    }
}
