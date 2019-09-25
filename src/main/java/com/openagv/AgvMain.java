package com.openagv;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.handlers.AccountHandler;

public class AgvMain {

    private final static Log logger = LogFactory.get();

    public static void doTask(String cmd) {
        AccountHandler.doHandler(cmd);
    }

}
