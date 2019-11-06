package com.openagv.core.interfaces;

import java.io.Serializable;

/**
 * 协议处理接口
 *
 * @author Laotang
 */
public interface IProrocol {
    /**
     * 回复协议
     * @param request
     */
    void reply(IRequest request);

    /**
     *
     * @param protocolString
     */
    <T> T toBean(String protocolString);
}
