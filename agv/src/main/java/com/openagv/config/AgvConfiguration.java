package com.openagv.config;

public class AgvConfiguration {

    public int commandQueueCapacity() {
        return 3;
    }

    public int sentQueueCapacity() {
        return 2;
    }

    public String rechargeOperation() {
        return "Charge";
    }
}
