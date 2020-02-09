package com.robot.mvc.core.interfaces;

import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.model.RepeatSendModel;

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
    boolean remove(IRequest request) throws RobotException;

    /**
     * 根据车辆ID删除重发队列里的所有响应对象
     *
     * @param vehicleId 车辆ID
     * @throws RobotException
     */
    void removeAll(String vehicleId) throws RobotException;
}
