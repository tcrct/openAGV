package com.makerwit.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.makerwit.core.component.Protocol;
import com.makerwit.numes.CmdKeyEnum;
import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.utils.CrcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * 协议对象工具
 * Created by laotang on 2019/12/22.
 */
public class ProtocolUtil {

    private static final Logger LOG =  LoggerFactory.getLogger(ProtocolUtil.class);

    /**
     * 根据报文内容构建协议对象
     *
     * @param telegramData 报文内容
     * @return Protocol 协议对象
     */
    public static Protocol buildProtocol(String telegramData) {
        if(!checkTelegramFormat(telegramData)) {
            throw new IllegalArgumentException("报文["+telegramData+"]格式不正确");
        }

        String[] telegramArray = StrUtil.split(telegramData, MakerwitEnum.SEPARATOR.getValue());
        if(ArrayUtil.isEmpty(telegramArray)) {
            throw new NullPointerException("构建协议对象时，报文内容主体不能为空");
        }

        for(String itemValue  : telegramArray){
            if(StrUtil.isEmpty(itemValue)) {
                throw new NullPointerException("协议报文的每个单元内容值不能为空");
            }
        }

       return new Protocol.Builder()
        .deviceId(telegramArray[1])
        .direction(telegramArray[2])
        .cmdKey(telegramArray[3])
        .params(telegramArray[4])
        .code(telegramArray[5])
        .build();

    }

    /**
     * 将协议对象转换为字符串
     * @param protocol 协议对象
     * @return 字符串
     */
    public static String converterString(Protocol protocol) {
        StringBuilder protocolStr = new StringBuilder();
        String rawStr = builderCrcString(protocol);
        protocolStr
                .append(rawStr)
                .append(buildCrc(rawStr))
                .append(MakerwitEnum.SEPARATOR.getValue())
                .append(MakerwitEnum.FRAME_END.getValue());
        return protocolStr.toString();
    }

    /**
     * 构建crc验证码
     * @param protocol 协议对象
     * @return crc验证码
     */
    public static String builderCrcString(Protocol protocol) {

        if (!checkProtocolValue(protocol)) {
            return "";
        }

        StringBuilder protocolString = new StringBuilder();
        protocolString.append(MakerwitEnum.FRAME_HEAD.getValue())
                .append(MakerwitEnum.SEPARATOR.getValue())
                .append(protocol.getDeviceId())
                .append(MakerwitEnum.SEPARATOR.getValue())
                .append(protocol.getDirection())
                .append(MakerwitEnum.SEPARATOR.getValue())
                .append(protocol.getCmdKey())
                .append(MakerwitEnum.SEPARATOR.getValue())
                .append(protocol.getParams())
                .append(MakerwitEnum.SEPARATOR.getValue());

        return protocolString.toString();
    }

    /**
     * 构建握手报文的code
     * 将方向反转再做成code返回
     * @param protocol
     * @return 握手报文code
     */
    public static String builderHandshakeCode(Protocol protocol) {
        if (!checkProtocolValue(protocol)) {
            return "";
        }

        String direction = "";
        if (MakerwitEnum.DOWN_LINK.getValue().equals(protocol.getDirection())) {
            direction = MakerwitEnum.UP_LINK.getValue();
        } else if (MakerwitEnum.UP_LINK.getValue().equals(protocol.getDirection())){
            direction = MakerwitEnum.DOWN_LINK.getValue();
        }

        Protocol newProtocol = new Protocol.Builder()
                .deviceId(protocol.getDeviceId())
                .cmdKey(protocol.getCmdKey())
                .direction(direction)
                .params(protocol.getParams())
                .build();

        return CrcUtil.CrcVerify_Str(builderCrcString(newProtocol));
    }

    /**
     * 检查报文对象值，如果为空值的话，则返回false
     * @param protocol
     * @return
     */
    private static boolean checkProtocolValue(Protocol protocol) {
        Objects.requireNonNull(protocol, "协议对象不能为空");

        Map<String,Object> protocolMap = BeanUtil.beanToMap(protocol);
        for (Iterator<Map.Entry<String,Object>> iterator = protocolMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String,Object> entry = iterator.next();
            if (null != entry.getValue()) {
                String value = entry.getValue().toString();
                if (StrUtil.isBlank(value) || "null".equalsIgnoreCase(value)) {
                    LOG.error("构建协议对象时，报文内容主体不能为空");
                    return false;
                }
            }
        }

        return true;
    }

    private static String buildCrc(String crc) {
        if (StrUtil.isNotBlank(crc)) {
            return CrcUtil.CrcVerify_Str(crc);
        }
        return "0000";
    }

    /**
     * 是否报文格式
     * @param telegramData
     * @return 正确返回true
     */
    public static boolean checkTelegramFormat(String telegramData) {
        return telegramData.contains(MakerwitEnum.FRAME_HEAD.getValue()) &&
                        telegramData.contains(MakerwitEnum.SEPARATOR.getValue()) &&
                        StrUtil.endWith(telegramData, MakerwitEnum.FRAME_END.getValue());
    }

    /**
     * 是否是业务协议，所有不是移动、上报卡号、预停车到位指令的协议，都是业务协议
     * @param cmdKey
     * @return
     */
    public static boolean isBusinessProtocol(String cmdKey) {
        return !isSetRoutProtocol(cmdKey) &&
                !isRptAcProtocol(cmdKey) &&
                !isRptRtpProtocol(cmdKey);
    }

    /**
     * 是否是Move协议，即车辆移动指令协议为状态协议
     * @param cmdKey 指令
     * @return 如果是返回true
     */
    public static boolean isSetRoutProtocol(String cmdKey) {
        return CmdKeyEnum.SETROUT.getValue().equalsIgnoreCase(cmdKey);
    }
    /**
     * 是否是rptac协议，即车辆上报卡号协议
     * @param cmdKey 指令
     * @return 如果是返回true
     */
    public static boolean isRptAcProtocol(String cmdKey) {
        return CmdKeyEnum.RPTAC.getValue().equalsIgnoreCase(cmdKey);
    }
    /**
     * 是否是rptrtp协议，即车辆预停车指令协议
     * @param cmdKey 指令
     * @return 如果是返回true
     */
    public static boolean isRptRtpProtocol(String cmdKey) {
        return CmdKeyEnum.RPTRTP.getValue().equalsIgnoreCase(cmdKey);
    }


    /**
     * 检查是否为业务协议的请求
     * 方向为s时为请求
     *
     * @param protocol 待检查的协议对象
     * @return 是则返回true，否则返回false
     */
    public static boolean isBusinessRequest(Protocol protocol) {
        requireNonNull(protocol, "报文协议对象不能为空");
        return ProtocolUtil.isBusinessProtocol(protocol.getCmdKey()) &&
                MakerwitEnum.UP_LINK.getValue().equalsIgnoreCase(protocol.getDirection());
    }

    /**
     * 检查是否为业务协议的响应
     *方向为r时为响应
     *
     * @param protocol 待检查的报文协议对象
     * @return 是则返回true，否则返回false
     */
    public static boolean isBusinessResponse(Protocol protocol) {
        requireNonNull(protocol, "报文协议对象不能为空");

        return ProtocolUtil.isBusinessProtocol(protocol.getCmdKey()) &&
                MakerwitEnum.DOWN_LINK.getValue().equalsIgnoreCase(protocol.getDirection());
    }


}
