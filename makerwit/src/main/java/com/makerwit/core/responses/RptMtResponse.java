package com.makerwit.core.responses;

import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.makerwit.utils.ProtocolUtil;
import com.robot.mvc.core.telegram.ActionResponse;
import com.robot.mvc.utils.CrcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by laotang on 2020/1/28.
 */
public class RptMtResponse extends ActionResponse {

    private static final Logger LOG = LoggerFactory.getLogger(RptMtResponse.class);

    public RptMtResponse(String deviceId, String param) {
        super(new Protocol.Builder()
                .deviceId(deviceId)
                .direction(MakerwitEnum.UP_LINK.getValue())
                .cmdKey(CmdKeyEnum.RPTMT.getValue())
                .params(param)
                .build());
    }

    // 传感器对象作参数时
    public RptMtResponse(String deviceId, Sensor sensor) {
        this(deviceId, sensor.toString());
        // 计算验证码
        Protocol protocol = new Protocol.Builder()
                .deviceId(deviceId)
                .cmdKey(cmd())
                .direction(MakerwitEnum.UP_LINK.getValue())
                .params(sensor.toString())
                .build();
        String crcString = ProtocolUtil.builderCrcString(protocol);
        String code = CrcUtil.CrcVerify_Str(crcString);
        protocol.setCode(code);
        sensor.setCode(code);
        LOG.info("待上传的传感器协议内容: {}, 握手验证码: {}", ProtocolUtil.converterString(protocol), code);
        // 加入到缓存
        Sensor.setSensor(deviceId, sensor);
    }

    @Override
    public String cmd() {
        return CmdKeyEnum.RPTMT.getValue();
    }
}
