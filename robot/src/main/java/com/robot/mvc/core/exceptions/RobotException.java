package com.robot.mvc.core.exceptions;

/**
 * Created by laotang on 2020/1/12.
 */
public class RobotException extends RuntimeException {

    public RobotException(String errMessage) {
        super(errMessage);
    }

    public RobotException(String errMessage, Throwable e) {
        super(errMessage, e);
    }
}
