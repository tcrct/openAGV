package com.robot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序启动Robot
 *
 * @author Laotang
 * @date 2020/2/1.
 * @since 1.0
 */
public class Application {
    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    private static Application application;

    public static Application duang() {
        if (application == null) {
            application = new Application();
        }
        return application;
    }

}
