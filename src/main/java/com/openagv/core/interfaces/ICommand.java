package com.openagv.core.interfaces;

public interface ICommand {

    /**
     * 命令
     * @return
     */
    String cmd();

    /**
     * 设备ID
     * @return
     */
    String deviceId();

}
