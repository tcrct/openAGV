package com.robot;

import com.robot.adapter.RobotCommAdapter;
import com.robot.contrib.netty.comm.NetChannelType;
import com.robot.mvc.core.interfaces.IComponents;
import com.robot.mvc.core.interfaces.IHandler;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.utils.SettingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by laotang on 2020/1/12.
 */
public class RobotContext {

    private static final Logger LOG = LoggerFactory.getLogger(RobotContext.class);

    private static final List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    // 是否需要应答回复
    private static Boolean isAnswer = null;
    // RequestKit发送的请求后，缓存到此Map，等待回复
    private static Map<String, LinkedBlockingQueue<IProtocol>> RESPONSE_PROTOCOL_MAP = new ConcurrentHashMap<>();
    // OpenAgv需要的组件接口
    private static IComponents components;
    // 车辆适配器集合
    private static Map<String, RobotCommAdapter> ADAPTER_MAP = new ConcurrentHashMap<>();


    /**
     * 取车辆适配器集合
     * @return
     */
    public static Map<String, RobotCommAdapter> getAdapterMap() {
        return ADAPTER_MAP;
    }

    /***
     * 根据key取出Adapter
     * @param key 车辆标识关键字
     */
    public static RobotCommAdapter getAdapter(String key) {
        return ADAPTER_MAP.get(key);
    }
    /**
     * 设置OpenAGV需要使用的第三方组件实例接口
     * @param components 第三方组件实例接口
     */
    public static void setRobotComponents(IComponents components) {
        RobotContext.components = components;
    }

    /**
     * 取第三方组件实例接口
     * @return 第三方组件实例接口
     */
    public static IComponents getRobotComponents() {
        return components;
    }

    /***
     * 取前置处理器
     * @return
     */
    public static List<IHandler> getBeforeHeandlerList() {
        return BEFORE_HEANDLER_LIST;
    }


    /**是否需要进行握手回复*/
    public static boolean isAnswer() {
        if (null == isAnswer) {
            isAnswer = SettingUtils.getBoolean("answer", true);
        }
        return isAnswer;
    }

    /**缓存RequestKit发出的请求，等待响应回复，key为crc验证码*/
    public static Map<String, LinkedBlockingQueue<IProtocol>> getResponseProtocolMap() {
        return RESPONSE_PROTOCOL_MAP;
    }

    /**
     * 设置通讯类型
     */
    private static NetChannelType CHANNEL_TYPE = null;
    public  static void setNetChannelType(NetChannelType channelType) {
        LOG.info("OpenAgv车辆适配器的网络渠道类型为: {}", channelType);
        CHANNEL_TYPE = channelType;
    }
    public static NetChannelType getNetChannelType() {
        return CHANNEL_TYPE;
    }
}


