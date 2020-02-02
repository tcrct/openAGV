package com.robot.contrib.netty.comm;

import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.IChannelManager;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.contrib.netty.comm.RunType;
import com.robot.contrib.netty.rxtx.RxtxServerManager;
import com.robot.contrib.netty.tcp.TcpClientManager;
import com.robot.contrib.netty.tcp.TcpServerManager;
import com.robot.contrib.netty.udp.UdpClientManager;
import com.robot.contrib.netty.udp.UdpServerManager;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.utils.RobotUtil;

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
    public static IChannelManager getChannelManager(RobotCommAdapter adapter) {
        // 如果是以服务器方式启动，则在初始化时完成
        String runType = RobotUtil.getRunType();
        NetChannelType channelType = RobotUtil.getNetChannelType();
        if (RunType.SERVER.name().equalsIgnoreCase(runType)) {
            return getServerChannelManager(adapter, channelType);
        } else if (RunType.CLIENT.name().equalsIgnoreCase(runType)) {
            return getClientChannelManager(adapter, channelType);
        } else {
            throw new RobotException("配置文件[run.type]参数值设置不正确，仅允许server/client两个选项");
        }
    }

    /***
     * 以服务器方式运行的管理器
     * @param adapter 车辆通讯适配器
     * @param channelType  网络渠道类型
     * @return
     */
    private static IChannelManager getServerChannelManager(RobotCommAdapter adapter, NetChannelType channelType) {
        if (NetChannelType.TCP.equals(channelType)) {
            return TcpServerManager.duang(adapter);
        } else if (NetChannelType.UDP.equals(channelType)) {
            return UdpServerManager.duang(adapter);
        } else if (NetChannelType.RXTX.equals(channelType)) {
            return RxtxServerManager.duang(adapter);
        }
        return null;
    }

    /**
     * 以客户端方式运行的管理器
     * @param adapter 车辆通讯适配器
     * @param channelType   网络渠道类型
     * @return
     */
    private static IChannelManager getClientChannelManager(RobotCommAdapter adapter, NetChannelType channelType) {
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
