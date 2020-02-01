package com.robot.config;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.apache.log4j.PropertyConfigurator;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;

import java.io.File;
import java.net.URL;

public class OpenAgvConfigure {

    private static final Log logger = LogFactory.get();
    private static OpenAgvConfigure agvConfigure;

    public static OpenAgvConfigure duang() {
        if (null == agvConfigure) {
            agvConfigure = new OpenAgvConfigure();
        }
        return agvConfigure;
    }

    private OpenAgvConfigure() {
        init();
    }

    private void init() {
        String configDir = "config"; // 只能是以config作为文件夹
        URL url = ClassUtil.getResourceURL(configDir);
        String classesPath = url.getPath();
        File dir = new File(classesPath);
        classesPath = dir.getParentFile().getAbsolutePath();
        logger.info("OpenTcsConfigure Path: {}", classesPath);
//        System.setProperty("java.util.logging.config.file", classesPath+File.separator+subDir+File.separator+"logging.config");
        System.setProperty("java.security.policy", classesPath + File.separator + configDir + File.separator + "java.policy");
        System.setProperty("opentcs.base", classesPath);
        System.setProperty("opentcs.home", ".");
        System.setProperty("splash", classesPath + File.separator + "bin" + File.separator + "splash-image.gif");
        System.setProperty("file.encoding", CharsetUtil.UTF_8.name());
        PropertyConfigurator.configure(classesPath + File.separator + "log4j.properties");
        logger.warn("OpenAgvConfigure init success");

    }
}
