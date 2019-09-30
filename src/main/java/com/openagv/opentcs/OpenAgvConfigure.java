package com.openagv.opentcs;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.openagv.core.interfaces.IDecomposeTelegram;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import java.io.File;
import java.net.URL;

public abstract class OpenAgvConfigure {

    private static final Log logger = LogFactory.get();
    private ConnectionEventListener connectionEventListener;
    private MessageToMessageDecoder decoder;
    private MessageToMessageEncoder encoder;

    public OpenAgvConfigure() {
        init();
    }

    private void init() {
        String subDir = "config";
        URL url = ClassUtil.getResourceURL(subDir);
        String configPath = url.getPath();
        File dir = new File(configPath);
        configPath = dir.getParentFile().getAbsolutePath();
        logger.info("OpenTcsConfigure Path: {}", configPath);
        System.setProperty("java.util.logging.config.file", configPath+File.separator+subDir+File.separator+"logging.config");
        System.setProperty("java.security.policy", configPath +File.separator+subDir+File.separator+ "java.policy");
        System.setProperty("opentcs.base", configPath);
        System.setProperty("opentcs.home", ".");
        System.setProperty("splash", configPath + File.separator+"bin"+File.separator+"splash-image.gif");
        System.setProperty("file.encoding", "UTF-8");
        logger.warn("OpenAgvConfigure init success");
    }

    public abstract ConnectionEventListener getConnectionEventListener();
    public abstract MessageToMessageDecoder getDecoder();
    public abstract MessageToMessageEncoder getEncoder();
    public abstract IDecomposeTelegram getDecomposeTelegram();


//    public static void main(String[] args) {
//        new OpenTcsConfigure();
//    }
}
