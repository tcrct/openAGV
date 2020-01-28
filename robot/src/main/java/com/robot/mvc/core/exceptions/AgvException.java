package com.robot.mvc.core.exceptions;

/**
 * Created by laotang on 2020/1/12.
 */
public class AgvException extends RuntimeException {

    public AgvException(String errMessage) {
        super(errMessage);
    }

    public AgvException(String errMessage, Throwable e) {
        super(errMessage, e);
    }
}
