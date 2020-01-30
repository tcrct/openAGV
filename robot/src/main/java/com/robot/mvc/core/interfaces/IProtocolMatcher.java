package com.robot.mvc.core.interfaces;

/**
 * 协议对象编码解码器匹配器接口
 * <p>
 * Created by laotang on 2020/1/12.
 */
public interface IProtocolMatcher {

    IProtocol encode(String message) throws Exception;

    String decode(IProtocol protocol) throws Exception;

}
