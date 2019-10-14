package com.openagv.core;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.openagv.core.interfaces.*;
import com.openagv.exceptions.AgvException;
import com.openagv.opentcs.OpenAgvConfigure;
import com.openagv.opentcs.adapter.CommAdapter;
import com.openagv.opentcs.enums.CommunicationType;
import com.openagv.opentcs.telegrams.TelegramQueueDto;
import com.openagv.route.Route;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import gnu.io.SerialPort;
import org.apache.log4j.Logger;
import org.opentcs.components.kernel.services.TCSObjectService;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 上下文及映射容器
 *
 * @author Laotang
 */
public class AppContext {

    private final static Logger logger = Logger.getLogger(AppContext.class);

    /**要进行依赖反转的类*/
    private final static Set<Class<?>> INJECT_CLASS_SET = new HashSet<>();
    /**要进行依赖反转的对象*/
    private final static Set<Object> INJECT_OBJECT_SET = new HashSet<>();
    /**路由映射*/
    private final static Map<String, Route> ROUTE_MAP = new HashMap<>();
    /**在执行Controller前的处理器链*/
    public static List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    /**在执行Controller后的处理器链*/
    public static List<IHandler> AFTER_HEANDLER_LIST = new ArrayList<>();
    /**插件*/
    private static final List<IPlugin> PLUGIN_LIST = new ArrayList<>();
    /**自定义指令操作集合*/
    private static final Map<String, IAction> CUSTOM_ACTION_QUEYE = new HashMap<>();
    /**插件开启回调*/
    private static final List<IEnable> PLUGIN_ENABLE_LIST = new ArrayList<>();

    /**guice的injector*/
    private static Injector injector;
    /**injector的Module集合*/
    private final static Set<Module> MODULES = new HashSet<>();



    public static void setGuiceInjector(Injector injector) {
        AppContext.injector = injector;
    }

    public static Injector getGuiceInjector() {
        return injector;
    }

    public static Set<Module> getModules() {
        return MODULES;
    }

    public static Set<Class<?>> getInjectClassSet() {
        return INJECT_CLASS_SET;
    }

    public static Set<Object> getInjectClassObjectSet() {
//        if(INJECT_OBJECT_SET.isEmpty()){
//            getInjectClassSet().forEach(new Consumer<Class<?>>() {
//                @Override
//                public void accept(Class<?> clazz) {
//                    if(ToolsKit.isInjectServiceClass(clazz)) {
//                        Object injectObj = AppContext.getGuiceInjector().getInstance(clazz);
//    //                    String key =ToolsKit.getControllerName(clazz);
//    //                    ROUTE_MAP.put(key.toLowerCase(), new Route(key, injectObj));
//                        INJECT_OBJECT_SET.add(injectObj);
//                    }
//                }
//            });
//        }
        return INJECT_OBJECT_SET;
    }


    public static Map<String, Route> getRouteMap() {
        return ROUTE_MAP;
    }

    public static List<IHandler> getBeforeHeandlerList() {
        return BEFORE_HEANDLER_LIST;
    }

    public static List<IHandler> getAfterHeandlerList() {
        return AFTER_HEANDLER_LIST;
    }

    public static void setAfterHeandlerList(List<IHandler> afterHeandlerList) {
        AFTER_HEANDLER_LIST = afterHeandlerList;
    }

    public static List<IPlugin> getPluginList() {
        return PLUGIN_LIST;
    }

    /**
     * 串口
     */
    private static SerialPort serialPort;
    public static void setSerialPort(SerialPort serialPort) {
        AppContext.serialPort = serialPort;
    }
    public static SerialPort getSerialPort() {
        return serialPort;
    }


//    private static final String TELEGRAM_SETTING_FIELD = "telegram.impl";
//    private static IDecomposeTelegram DECOMPOSE_TELEGRAM;
//    public static void setDecomposeTelegram(IDecomposeTelegram decomposeTelegram){
//        DECOMPOSE_TELEGRAM = decomposeTelegram;
//    }
//    public static IDecomposeTelegram getDecomposeTelegram (){
//        if(ToolsKit.isEmpty(DECOMPOSE_TELEGRAM)) {
//            String telegramImpl = SettingUtils.getString(TELEGRAM_SETTING_FIELD);
//            if(ToolsKit.isEmpty(telegramImpl)) {
//                DECOMPOSE_TELEGRAM new NullPointerException("请先实现"+ IDecomposeTelegram.class.getName()+"接口，并在app.setting文件里["+TELEGRAM_SETTING_FIELD+"]添加接口的实现类路径");
//            }
//            DECOMPOSE_TELEGRAM = ReflectUtil.newInstance(telegramImpl);
//        }
//        return DECOMPOSE_TELEGRAM;
//    }


    public static List<IEnable> getPluginEnableList() {
        return PLUGIN_ENABLE_LIST;
    }

    /*
    private static Object channelManagerObj;
    public static void setChannelManager(Object channelManager) {
        channelManagerObj =channelManager;
    }
    // 初始化车辆渠道管理器
    public static void channelManagerInitialize(){
        java.util.Objects.requireNonNull(channelManagerObj, "渠道管理对象不能为空");
        if(channelManagerObj instanceof TcpClientChannelManager) {
            TcpClientChannelManager channelManager = (TcpClientChannelManager)channelManagerObj;
            if(!channelManager.isInitialized()) {
                channelManager.initialize();
            }
        }
        else if(channelManagerObj instanceof UdpServerChannelManager) {
            UdpServerChannelManager channelManager = (UdpServerChannelManager)channelManagerObj;
            if(!channelManager.isInitialized()) {
                channelManager.initialize();
            }
        }
        else {

        }
    }
    */


    private static String INVOKE_CLASS_TYPE ;
    public static String getInvokeClassType() {
        if(null == INVOKE_CLASS_TYPE) {
            INVOKE_CLASS_TYPE = SettingUtils.getString("invoke.class", ToolsKit.SERVICE_FIELD);
        }
        return INVOKE_CLASS_TYPE;
    }

    /**
     * 下达车辆移动指令的key
     */
    private static String STATE_REQUEST_CMD_KEY;
    public static String getStateRequestCmdKey() {
        if(null == STATE_REQUEST_CMD_KEY) {
            STATE_REQUEST_CMD_KEY = SettingUtils.getString("state.request.cmd");
            if(ToolsKit.isEmpty(STATE_REQUEST_CMD_KEY)) {
                throw new NullPointerException("该值不能为空，请先在app.setting设置[state.request.target]值，该值是车辆移动指令的标识！");
            }
        }
        return STATE_REQUEST_CMD_KEY;
    }

    private static OpenAgvConfigure CONFIGURE;
    public static void setAgvConfigure(OpenAgvConfigure configure) {
        if(null == CONFIGURE) {
            CONFIGURE = configure;
        }
    }
    public static OpenAgvConfigure getAgvConfigure() {
        return CONFIGURE;
    }

    private static Boolean isHandshakeListener = null;
    public static boolean isHandshakeListener() {
        if(null == isHandshakeListener) {
            isHandshakeListener = (null != getAgvConfigure().getHandshakeListener());
        }
        return isHandshakeListener;
    }

    private static CommunicationType COMMUNICATION_TYPE_ENUM = CommunicationType.SERIALPORT;
    public static void setCommunicationType(CommunicationType typeEnum) {
        COMMUNICATION_TYPE_ENUM = typeEnum;
    }
    public static CommunicationType getCommunicationType() {
        return COMMUNICATION_TYPE_ENUM;
    }

    /**
     * 自定义的指令队列集合
     * @return
     */
    public static Map<String, IAction> getCustomActionsQueue() {
        return CUSTOM_ACTION_QUEYE;
    }

    /**
     * 通讯适配器
     */
    private static CommAdapter COMM_ADAPTER;
    public static void setCommAdapter(CommAdapter commAdapter) {
        COMM_ADAPTER = commAdapter;
    }
    public static CommAdapter getCommAdapter() {
        return COMM_ADAPTER;
    }

    /**
     * 大杀器----TCS的对象服务器
     */
    public static TCSObjectService getOpenTcsObjectService(){
        return getCommAdapter().getObjectService() ;
    }


    /***握手电报队列，key为车辆，Value为所有需要响应的报文对象
     * 用于消息握手机制，必须进行消息握手确认处理，如握手不成功，则阻塞该车辆的消息发送，直至握手确认成功
     * 确认成功后，会移除顶部位置的报文对象
     * */
    private final static Map<String, LinkedBlockingQueue<TelegramQueueDto>> HANDSHAKE_TELEGRAM_QUEUE = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 添加到队列
     * @param queueDto 队列对象
     */
    public static void setHandshakeTelegramQueue(TelegramQueueDto queueDto) {
        if(ToolsKit.isEmpty(queueDto)) {
            throw new NullPointerException("队列对象不能为空");
        }
        String deviceId = queueDto.getDeviceId();
        String key = queueDto.getHandshakeKey();
        IResponse response = queueDto.getResponse();
        if(ToolsKit.isEmpty(deviceId)){
            throw new NullPointerException("设备ID不能为空");
        }
        if(ToolsKit.isEmpty(key)){
            throw new NullPointerException("标识字段不能为空，一般是指验证码之类的唯一标识字段");
        }
        if(ToolsKit.isEmpty(response)){
            throw new NullPointerException("返回的对象不能为空");
        }
        LinkedBlockingQueue<TelegramQueueDto> queue =  HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if(ToolsKit.isEmpty(queue)) {
            queue = new LinkedBlockingQueue<>();
        }
        // 添加到队列
        queue.add(queueDto);
        HANDSHAKE_TELEGRAM_QUEUE.put(deviceId, queue);
    }
    /**
     * 移除元素
     * @param deviceId 设备ID
     * @param key 标识字段，用于确认握手关系
     */
    public static void removeTelegramQueue(String deviceId, String key) {
        java.util.Objects.requireNonNull(deviceId, "设备ID不能为空");
        java.util.Objects.requireNonNull(key, "标识字段不能为空，一般是指验证码之类的唯一标识字段");
        LinkedBlockingQueue<TelegramQueueDto> queue =  HANDSHAKE_TELEGRAM_QUEUE.get(deviceId);
        if(ToolsKit.isEmpty(queue)) {
            throw new AgvException("handshake queue is null");
        }
        TelegramQueueDto toBeDeleteDto = queue.peek();
        if(ToolsKit.isEmpty(toBeDeleteDto)) {
            throw new AgvException("TelegramQueueDto is null");
        }
        String handshakeKey = toBeDeleteDto.getHandshakeKey();
        if(ToolsKit.isNotEmpty(queue) && ToolsKit.isNotEmpty(handshakeKey) && handshakeKey.equals(key)) {
            //先复制 ??
            TelegramQueueDto queueDto = new TelegramQueueDto(toBeDeleteDto);
            // 再移除第一位元素对象
            queue.remove();
            logger.info("remove vehicle["+deviceId+"] queueDto["+queueDto.toString()+"] is success!");
            // 指令队列中移除后再发送下一个指令
            ICallback callback =queueDto.getCallback();
            String requestId = queueDto.getReqeustId();
            if(ToolsKit.isNotEmpty(callback) && ToolsKit.isNotEmpty(requestId)) {
                // 回调机制，告诉系统这条指令可以完结了。
                try {
                    callback.call(deviceId, requestId);
                } catch (Exception e) {
                    throw new AgvException(e.getMessage(), e);
                }
            }
        }
        if(ToolsKit.isEmpty(handshakeKey)) {
            throw new AgvException("握手key不能为空");
        }
        if(!handshakeKey.equals(key)) {
            throw new AgvException("提交上来的报文handshakeKey["+key+"]与系统队列中handshakeKey["+handshakeKey+"]不一致！");
        }
    }
//    /***
//     * 返回集合中指定队列中第一个元素
//     * @param deviceId 设备ID
//     */
//    public static IResponse getFirstResponseByQueue(String deviceId) {
//        java.util.Objects.requireNonNull(deviceId, "设备ID不能为空");
//        LinkedBlockingQueue<Map<String, IResponse>> queue = TELEGRAM_QUEUE.get(deviceId);
//        if(ToolsKit.isEmpty(queue)) {
//            Map<String, IResponse> map = queue.peek();
//            if (ToolsKit.isNotEmpty(map)) {
//                return map.values().iterator().next();
//            }
//        }
//        return null;
//    }

    /***返回报文集合队列*/
    public static  Map<String, LinkedBlockingQueue<TelegramQueueDto>> getHandshakeTelegramQueue() {
        return HANDSHAKE_TELEGRAM_QUEUE;
    }

}
