package com.openagv.config;

/**
 * @blame opentcs
 * @author Laotang
 * @since 1.0
 */
public class AgvConfiguration {

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
