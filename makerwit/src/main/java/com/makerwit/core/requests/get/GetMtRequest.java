package com.makerwit.core.requests.get;

import com.robot.service.common.ActionRequest;

/**
 * 查询物料请求
 *
 * @author Laotang
 */
public class GetMtRequest extends ActionRequest {

    public GetMtRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getmt";
    }
}
