package com.robot.mvc.core.interfaces;

import com.robot.mvc.core.exceptions.RobotException;

/**
 * Created by laotang on 2020/1/30.
 */
public interface IRequestCallback {

    void call(IProtocol protocol) throws RobotException;

}
