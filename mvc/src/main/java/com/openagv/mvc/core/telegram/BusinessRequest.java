package com.openagv.mvc.core.telegram;

import com.openagv.mvc.core.enums.ReqType;
import com.openagv.mvc.core.interfaces.IProtocol;

/**
 * 业务请求，一般是指协议请求
 * 指由车辆或设备发送到调度系统的请求
 *
 * Created by laotang on 2020/1/12.
 */
public class BusinessRequest extends BaseRequest {

    public BusinessRequest(IProtocol protocol) {
        super(ReqType.BUSINESS, protocol);
    }

}
