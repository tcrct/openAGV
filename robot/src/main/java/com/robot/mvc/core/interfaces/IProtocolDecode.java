package com.robot.mvc.core.interfaces;

/**
 * 协议对象解码器接口
 *
 * Created by laotang on 2020/1/12.
 */
public interface IProtocolDecode {

    IProtocol decode(String message) throws Exception;

}
