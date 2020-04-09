package com.robot.mvc.core.exceptions;

import com.robot.mvc.core.interfaces.IException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by laotang on 2020/1/12.
 */
public class RobotException extends RuntimeException implements IException {

    private static final Logger LOG = LoggerFactory.getLogger(RobotException.class);

    private int code = 1;

    public RobotException(String errMessage) {
        super(errMessage);
        LOG.info(errMessage);
    }

    public RobotException(String errMessage, Throwable e) {
        super(errMessage, e);
        LOG.info(errMessage, e);
    }

    public RobotException(IException enums) {
        super(enums.getMessage());
        code = enums.getCode();
        LOG.info("code: {}, message : {}", code, enums.getMessage());
        save(code, enums.getMessage());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private void save(int code, String message) {
        // todo 持久化
    }
}
