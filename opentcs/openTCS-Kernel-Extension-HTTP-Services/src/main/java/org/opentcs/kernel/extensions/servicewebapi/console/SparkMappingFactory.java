package org.opentcs.kernel.extensions.servicewebapi.console;

import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.IController;
import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.IWebSocket;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * SparkMapping工厂类
 * 1，在Duang链式设置WebSocket时用
 * 2，在程序启动时，在IocHelper进行注入到Map
 *
 * @author Laotang
 */
public class SparkMappingFactory {

    //*****************Controller*******************//

    private static final Map<String, IController> controllerMap = new HashMap<>();
    private static final Map<String, Method> METHOD_MAP = new HashMap<>();

    public static void setController(String controllerName, IController controller) {
        controllerMap.put(controllerName, controller);
    }

    public static IController getController(String controllerName) {
        return controllerMap.get(controllerName);
    }

    public static Map<String, IController> getControllerMap() {
        return controllerMap;
    }

    public static Map<String, Method> getMethodMap() {
        return METHOD_MAP;
    }


    //*****************WebSocket*******************//

    private static final Map<String, Class<? extends IWebSocket>> WEB_SOCKET_MAP = new HashMap<>();

    public static Map<String, Class<? extends IWebSocket>> getWebSocketMap() {
        return WEB_SOCKET_MAP;
    }

}
