package com.robot.config;

/**
 * Robot配置
 *
 * @author Laotang
 * @blame opentcs
 * @since 1.0
 */
public class RobotConfiguration {

    /**
     * 命令队列容量
     *
     * @return
     */
    public int commandQueueCapacity() {
        return 1;
    }

    /**
     * @return
     */
    public int sentQueueCapacity() {
        return 1;
    }

    /***
     * 充电动作标识
     * @return
     */
    public String rechargeOperation() {
        return "Charge";
    }

}
