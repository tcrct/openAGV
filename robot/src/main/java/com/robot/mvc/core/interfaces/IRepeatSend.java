package com.robot.mvc.core.interfaces;

import com.robot.mvc.core.exceptions.RobotException;

/**
 * 重复发接报文接口
 * 用于握手处理，以确保报文到达车辆
 *
 * @author Laotang
 */
public interface IRepeatSend {

    /**
     * 添加需要重复发送的响应对象
     * @param response
     */
    void add(IResponse response) throws RobotException;

    /**
     * 根据请求对象删除需要重复发送的响应对象
     * @param request 请求对象
     * @return 删除成功返回true
     */
    void remove(IRequest request) throws RobotException;

}
