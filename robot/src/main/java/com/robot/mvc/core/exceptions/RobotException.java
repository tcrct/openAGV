package com.robot.mvc.core.exceptions;

import com.robot.mvc.core.interfaces.IException;

/**
 * Created by laotang on 2020/1/12.
 */
public class RobotException extends RuntimeException {

    private int code = 1;

    public RobotException(String errMessage) {
        super(errMessage);
    }

    public RobotException(String errMessage, Throwable e) {
        super(errMessage, e);
    }

    public RobotException(IException enums) {
        super(enums.getMessage());
        code = enums.getCode();
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
