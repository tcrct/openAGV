package com.openagv.plugins.serialport;

/**
 * 串口存在有效数据监听
 */
public interface DataAvailableListener {
    /**
     * 串口存在有效数据
     */
    void dataAvailable();
}
