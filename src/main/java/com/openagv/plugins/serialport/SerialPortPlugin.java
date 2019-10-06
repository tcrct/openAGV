package com.openagv.plugins.serialport;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AgvResult;
import com.openagv.core.AppContext;
import com.openagv.core.Main;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.opentcs.enums.CommunicationType;
import com.openagv.opentcs.model.Telegram;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import gnu.io.SerialPort;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * 串口插件类
 *
 * @author Laotang
 */
public class SerialPortPlugin implements IPlugin, IEnable, ITelegramSender {

    private static final Log logger = LogFactory.get();
    /**串口名称*/
    private String serialPortName;
    /**获取波特率，默认为38400*/
    private int baudrate;
    /**事件回调*/
    private ConnectionEventListener eventListener;


    public SerialPortPlugin() {
        this(SettingUtils.getStringByGroup("name", CommunicationType.SERIALPORT.name().toLowerCase(), "COM6"),
                SettingUtils.getInt("baudrate", CommunicationType.SERIALPORT.name().toLowerCase(), 38400),
                SettingUtils.getStringsToSet("broadcast", CommunicationType.SERIALPORT.name().toLowerCase())
        );
    }

    public SerialPortPlugin(String portName, int baudrate, Set<String> set) {
        serialPortName = portName;
        this.baudrate = baudrate;
        AppContext.setBroadcastFlag(set);
    }

    @Override
    public void start() throws Exception {
        List<String> mCommList = SerialPortManager.findPorts();

        if(ToolsKit.isEmpty(mCommList)) {
            throw new NullPointerException("没有找到可用的串串口！");
        }
        if(!mCommList.contains(serialPortName)) {
            throw new IllegalArgumentException("指定的串口名称["+serialPortName+"]与系统允许使用的不符");
        }
        if(baudrate == 0) {
            throw new IllegalArgumentException("串口波特率["+baudrate+"]没有设置");
        }
        try {
            AppContext.setSerialPort(SerialPortManager.openPort(serialPortName, baudrate));
        } catch (Exception e) {
            throw new RuntimeException("打开串口时失败，名称["+serialPortName+"]， 波特率["+baudrate+"], 串口可能已被占用！");
        }
        eventListener = AppContext.getAgvConfigure().getConnectionEventListener();
        if(ToolsKit.isEmpty(eventListener)) {
            throw new NullPointerException("事件监听器没有实现，请先实现");
        }
        AppContext.setCommunicationType(CommunicationType.SERIALPORT);
    }


    private String readTelegram(SerialPort serialPort) {
        java.util.Objects.requireNonNull(serialPort, "串口对象不能为null");
        byte[] data = null;
        try {
            // 读取串口数据
            data = SerialPortManager.readFromPort(serialPort);
            // 以字符串的形式接收数据
            return new String(data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public Object enable() {
        final SerialPort serialPort = AppContext.getSerialPort();
        if(null == serialPort) {
            return false;
        }
        eventListener.onConnect();
        SerialPortManager.addListener(serialPort, new DataAvailableListener() {
            @Override
            public void dataAvailable() {
                String telegram = readTelegram(serialPort);
                eventListener.onIncomingTelegram(telegram);
//                IResponse response = ToolsKit.sendCommand(new OrderRequest(telegram));

//                Telegram responseTelegram =getTemplate().builderTelegram(telegram);
//                if(ToolsKit.isEmpty(responseTelegram)) {
//                    return;
//                }
//                logger.info("串口接收到的报文：" + telegram);
//                logger.info("业务处理后到的报文：" + response);

//                if(!getTelegramMatcher().tryMatchWithCurrentRequestTelegram(responseTelegram)) {
//                    // 如果不匹配，则忽略该响应或关闭连接
//                    return;
//                }
//                /**检查并更新车辆状态，位置点*/
//                checkForVehiclePositionUpdate(responseTelegram);
//                /**在执行上面更新位置的方法后再检查是否有下一条请求需要发送*/
//                getTelegramMatcher().checkForSendingNextRequest();
            }
        });
        logger.warn("串口[{}]启动成功！波特率为[{}]", serialPortName, baudrate);
        return serialPort;
    }

    /**
     * 广播电报到设备
     * @param response The {@link IResponse} to be sent.
     */
    @Override
    public void sendTelegram(IResponse response) {
        if(null == response) {
            return;
        }
        SerialPortManager.sendToPort(AppContext.getSerialPort(), response.toString().getBytes());
    }
}
