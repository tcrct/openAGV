package com.makerwit.core.requests.get;

import com.robot.service.common.ActionRequest;

/**
 * 查询状态信息
 *
 * @author Laotang
 */
public class GetErrRequest extends ActionRequest {

    public GetErrRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "geterr";
    }
}
