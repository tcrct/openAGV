package com.openagv.opentcs.telegrams;

import cn.hutool.core.util.IdUtil;
import com.openagv.core.interfaces.IRequest;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.LongAdder;

/**
 * 请求的抽象类
 *
 * @author Laotang
 */
public abstract class AbsRequest implements IRequest {

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskAttribute.baseDirectory = null;
    }

    /**请求ID*/
    private String requestId = IdUtil.objectId();
    /**目标路径映射*/
    protected String target;
    /**目标类*/
    protected String deviceId;
    /**Order请求计数器*/
    private static final LongAdder orderRequestCount = new LongAdder();
    /**State请求计数器*/
    private static final LongAdder stateRequestCount = new LongAdder();
    /**扩展参数*/
    private Map<String, Object> paramMap = new HashMap<>();
    /**报文的原始字符串*/
    protected String originalTelegram;
    /**协议对象*/
    private Serializable bean;
    /**类似标识符，区分request/response对象*/
    protected String type;

    public AbsRequest(TelegramType type) {
        if (TelegramType.ORDER.equals(type)) {
            orderRequestCount.increment();
        } else if(TelegramType.STATE.equals(type)){
            stateRequestCount.increment();
        }
    }

    public enum TelegramType {
        ORDER, STATE
    }

    /**服务器启动后的总请求次数*/
    public Long getRequestCount(TelegramType type) {
        return TelegramType.ORDER.equals(type) ? orderRequestCount.longValue() : stateRequestCount.longValue();
    }

    @Override
    public String getRequestId() {
        return requestId;
    }
    @Override
    public Map<String,Object> getPropertiesMap() { return paramMap;}
    @Override
    public String getOriginalTelegram() {
        return originalTelegram;
    }
    @Override
    public String getCmdKey(){
        return target;
    }
    @Override
    public String getDeviceId(){
        return deviceId;
    }
    public void setProtocol(Serializable bean) {
        this.bean = bean;
    }
    @Override
    public Serializable getProtocol(){
        return bean;
    }

    /**
     * 目标请求路径，即协议指令里的功能命令
     */
    public abstract void setCmdKey(String target);

    /**设备、车辆ID*/
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    @Override
    public abstract String getRequestType();

}
