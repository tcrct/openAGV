package com.robot.mvc.core.telegram;

import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.interfaces.IProtocol;

/**
 * 业务请求，一般是指协议请求
 * 指由车辆或设备发送到调度系统的请求
 *
 * @author Laotang
 * @date 2020/1/12.
 * @since 1.0
 */
public class BusinessRequest extends BaseRequest {

    /**
     * 构造方法
     *
     * @param raw      接收到的协议字符串内容
     * @param protocol 协议对象
     */
    public BusinessRequest(String raw, IProtocol protocol) {
        super(ReqType.BUSINESS, protocol);
        setRawContent(raw);
    }


}
