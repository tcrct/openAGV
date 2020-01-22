package com.openagv;

import com.openagv.mvc.core.interfaces.IComponents;
import com.openagv.mvc.core.interfaces.IHandler;
import com.openagv.mvc.core.interfaces.IProtocol;
import com.openagv.mvc.utils.SettingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by laotang on 2020/1/12.
 */
public class AgvContext {

    private static final List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    //
    private static Boolean isAnswer = null;
    private static Map<String, LinkedBlockingQueue<IProtocol>> RESPONSE_PROTOCOL_MAP = new ConcurrentHashMap<>();
    private static IComponents components;



    /**
     * 设置OpenAGV需要使用的第三方组件实例接口
     * @param components 第三方组件实例接口
     */
    public static void setOpenAgvComponents(IComponents components) {
        AgvContext.components =  components;
    }

    /**
     * 取第三方组件实例接口
     * @return 第三方组件实例接口
     */
    public static IComponents getOpenAgvComponents() {
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
}


