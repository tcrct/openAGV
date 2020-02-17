package com.robot.mvc.core.interfaces;

import com.robot.mvc.core.exceptions.RobotException;

/**
 * 处理器接口
 * Created by laotang on 2020/1/12.
 */
public interface IHandler {

    /**
     * 处理方法
     *
     * @param target   协议指令
     * @param request  请求对象
     * @param response 返回对象
     * @return 处理成功返回true
     * @Exception RobotException
     */
    boolean doHandler(String target, IRequest request, IResponse response) throws RobotException;

}
