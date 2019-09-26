package com.openagv.opentcs;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.io.File;
import java.net.URL;

public class OpenTcsConfigure {

    private static final Log logger = LogFactory.get();

    public static void init() {
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
    }

//    public static void main(String[] args) {
//        new OpenTcsConfigure();
//    }
}
