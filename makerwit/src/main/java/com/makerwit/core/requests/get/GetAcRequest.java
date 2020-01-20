package com.makerwit.core.requests.get;

import com.robot.service.common.ActionRequest;

/**
 * 查询RFID卡
 *
 * @author Laotang
 */
public class GetAcRequest extends ActionRequest {

    public GetAcRequest(String deviceId, String params) {
        super(deviceId, params);
    }

    @Override
    public String cmd() {
        return "getac";
    }


}
