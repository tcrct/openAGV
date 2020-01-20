package com.makerwit.core.requests.get;

import com.robot.service.common.ActionRequest;

/**
 * 查询车辆速度值
 * 速度值范围：0-100
 *
 * @author Laotang
 */
public class GetSpdRequest extends ActionRequest {

    public GetSpdRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getspd";
    }
}
