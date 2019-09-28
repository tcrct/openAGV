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
import com.openagv.opentcs.model.Telegram;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import gnu.io.SerialPort;

import java.awt.*;
import java.util.List;

/**
 * 串口插件类
 *
 * @author Laotang
 */
public class SerialPortPlugin implements IPlugin, IEnable, ITelegramSender {

    private static final Log logger = LogFactory.get();

    @Override
    public void start() throws Exception {
        List<String> mCommList = SerialPortManager.findPorts();
        if(ToolsKit.isEmpty(mCommList)) {
            throw new NullPointerException("没有找到可用的串串口！");
        }

        String serialPortName = SettingUtils.getString("serialport.name", "COM6");
        if(!mCommList.contains(serialPortName)) {
            throw new IllegalArgumentException("指定的串口名称["+serialPortName+"]与系统允许使用的不符");
        }

        // 获取波特率，默认为38400
        int baudrate = SettingUtils.getInt("serialport.baudrate", 38400);
        try {
            AppContext.setSerialPort(SerialPortManager.openPort(serialPortName, baudrate));
        } catch (Exception e) {
            throw new RuntimeException("打开串口时失败，名称["+serialPortName+"]， 波特率["+baudrate+"]");
        }
        logger.warn("串口[{}]启动成功！波特率为[{}]", serialPortName, baudrate);
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
        SerialPortManager.addListener(serialPort, new DataAvailableListener() {
            @Override
            public void dataAvailable() {
                String telegram = readTelegram(serialPort);
                AgvResult result = ToolsKit.sendCommand(new Telegram(telegram));

//                Telegram responseTelegram =getTemplate().builderTelegram(telegram);
//                if(ToolsKit.isEmpty(responseTelegram)) {
//                    return;
//                }
                logger.info("串口接收到的报文：" + telegram);
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
        logger.info("开启串口渠道管理器[{}]成功!", serialPort.getName());
        return serialPort;
    }

    @Override
    public void sendTelegram(IResponse telegram) {

    }
}
