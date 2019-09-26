package com.openagv.plugins.serialport;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.AppContext;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import java.util.List;

/**
 * 串口插件类
 *
 * @author Laotang
 */
public class SerialPortPlugin implements IPlugin {

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

        listener();
        logger.warn("串口[{}]启动成功！波特率为[{}]", serialPortName, baudrate);
    }

    private void listener() {
        SerialPortManager.addListener(AppContext.getSerialPort(), new DataAvailableListener() {
            @Override
            public void dataAvailable() {
                String telegram = readTelegram4SerialPort();
                Telegram responseTelegram =getTemplate().builderTelegram(telegram);
                if(ToolsKit.isEmpty(responseTelegram)) {
                    return;
                }
                logger.info("串口接收到的报文：" + telegram);
                if(!getTelegramMatcher().tryMatchWithCurrentRequestTelegram(responseTelegram)) {
                    // 如果不匹配，则忽略该响应或关闭连接
                    return;
                }
                /**检查并更新车辆状态，位置点*/
                checkForVehiclePositionUpdate(responseTelegram);
                /**在执行上面更新位置的方法后再检查是否有下一条请求需要发送*/
                getTelegramMatcher().checkForSendingNextRequest();
            }
        });
    }

}
