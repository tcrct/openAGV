package com.openagv.core.command;

import com.openagv.opentcs.model.Telegram;

public abstract class Command {

    public abstract String execute(Telegram telegram);



}
