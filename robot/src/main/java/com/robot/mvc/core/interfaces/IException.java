package com.robot.mvc.core.interfaces;

/**
 * Created by laotang on 2020/2/3.
 */
public interface IException {

    int FAIL_CODE = 1;
    String FAIL_MESSAGE = "FAIL";

    int SUCCESS_CODE = 0;
    String SUCCESS_MESSAGE = "SUCCESS";

    int getCode();

    String getMessage();

}
