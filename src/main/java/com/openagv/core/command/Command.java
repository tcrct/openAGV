package com.openagv.core.command;

import com.openagv.core.AgvResult;
import com.openagv.opentcs.model.Telegram;

public abstract class Command {

    public abstract AgvResult execute(Telegram telegram);



}
