package com.openagv.core;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.openagv.core.interfaces.IEnable;
import com.openagv.core.interfaces.IHandler;
import com.openagv.core.interfaces.IPlugin;
import com.openagv.opentcs.OpenAgvConfigure;
import com.openagv.opentcs.adapter.CommAdapter;
import com.openagv.route.Route;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import gnu.io.SerialPort;
import org.opentcs.components.kernel.services.TCSObjectService;

import java.util.*;

/**
 * 上下文及映射容器
 *
 * @author Laotang
 */
public class AppContext {

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



}
