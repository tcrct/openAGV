package com.robot.config;

/**
 * Robot配置
 *
 * @blame opentcs
 * @author Laotang
 * @since 1.0
 */
public class RobotConfiguration {

    public int commandQueueCapacity() {
        return 100;
    }

    public int sentQueueCapacity() {
        return 100;
    }

    public String rechargeOperation() {
        return "Charge";
    }

}
