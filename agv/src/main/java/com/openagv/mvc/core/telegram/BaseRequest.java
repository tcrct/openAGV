package com.openagv.mvc.core.telegram;

import cn.hutool.core.util.IdUtil;
import com.openagv.adapter.AgvCommAdapter;
import com.openagv.mvc.core.enums.ReqType;
import com.openagv.mvc.core.interfaces.IProtocol;
import com.openagv.mvc.core.interfaces.IRequest;

import java.util.Objects;

/**
 * Created by laotang on 2020/1/12.
 */
public class BaseRequest implements IRequest, java.io.Serializable {

    /**请求ID*/
    protected String id;
    /**协议对象*/
    protected IProtocol protocol;
    /**请求对象类型枚举*/
    protected ReqType reqType;
    /**协议原文字符串*/
    protected String rawContent;
    /**车辆适配器，每一个请求里都必须包含*/
    protected AgvCommAdapter adapter;

    public BaseRequest(ReqType reqType, IProtocol protocol) {
        Objects.requireNonNull(reqType, "请求对象枚举值不能为空");
//        Objects.requireNonNull(protocol, "协议对象不能为空");
        setId(IdUtil.objectId());
        setProtocol(protocol);
        setReqType(reqType);
    }

    @Override
    public AgvCommAdapter getAdapter() {
        Objects.requireNonNull(protocol, "适配器对象不能为空");
        return adapter;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setProtocol(IProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public IProtocol getProtocol() {
        return protocol;
    }

    @Override
    public void setReqType(ReqType reqType) {
        this.reqType = reqType;
    }

    @Override
    public ReqType getReqType() {
        return reqType;
    }

    @Override
    public String getRawContent() {
        return rawContent;
    }

    @Override
    public void setRawContent(String raw) {
        this.rawContent = raw;
    }
}
