package com.openagv.core.command;

import com.openagv.core.AgvResult;
import com.openagv.core.interfaces.IRequest;
import com.openagv.opentcs.model.Telegram;

public abstract class Command {

    public abstract <T> T execute(IRequest request);

}
