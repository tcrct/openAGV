package com.makerwit.core.requests.get;

import com.robot.service.common.ActionRequest;

/**
 * 查下磁导传感器导通状态
 *
 * @author Laotang
 */
public class GetMagRequest extends ActionRequest {

    public GetMagRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getmag";
    }
}
