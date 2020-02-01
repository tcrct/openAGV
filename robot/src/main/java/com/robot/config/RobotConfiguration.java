package com.robot.config;

/**
 * Robot配置
 *
 * @blame opentcs
 * @author Laotang
 * @since 1.0
 */
public class RobotConfiguration {

    /**
     * 命令队列容量
     *
     * @return
     */
    public int commandQueueCapacity() {
        return 100;
    }

    /**
     *
     * @return
     */
    public int sentQueueCapacity() {
        return 100;
    }

    /***
     * 充电动作标识
     * @return
     */
    public String rechargeOperation() {
        return "Charge";
    }

}
