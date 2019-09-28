package com.openagv.opentcs.model;

import org.opentcs.drivers.vehicle.MovementCommand;

public class Telegram {

    private String agvTelegramString;
    private MovementCommand cmd;
    private ProcessModel processModel;

    public Telegram(String agvTelegramString) {
        this.agvTelegramString = agvTelegramString;
    }

    public Telegram(MovementCommand cmd, ProcessModel processModel) {
        this.cmd = cmd;
        this.processModel = processModel;
    }

    public String getAgvTelegramString() {
        return agvTelegramString;
    }

    public void setAgvTelegramString(String agvTelegramString) {
        this.agvTelegramString = agvTelegramString;
    }

    public MovementCommand getCmd() {
        return cmd;
    }

    public void setCmd(MovementCommand cmd) {
        this.cmd = cmd;
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }

}
