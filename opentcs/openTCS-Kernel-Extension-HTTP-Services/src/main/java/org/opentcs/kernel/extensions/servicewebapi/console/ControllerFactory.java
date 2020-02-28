package org.opentcs.kernel.extensions.servicewebapi.console;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller工厂类
 * 在程序启动时，在IocHelper进行注入到Map
 *
 * @author Laotang
 */
public class ControllerFactory {

    private static final Map<String, IController> controllerMap = new HashMap<>();

    public static void setController(String controllerName, IController controller) {
        controllerMap.put(controllerName, controller);
    }

    public static IController getController(String controllerName) {
        return controllerMap.get(controllerName);
    }

    public static Map<String, IController> getControllerMap() {
        return controllerMap;
    }
}
