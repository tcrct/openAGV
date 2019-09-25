package com.openagv.tools;

import com.openagv.core.annotations.Controller;

public class ToolsKit {

    private static final String CONTROLLER_FIELD = "Controller";

    public static String getControllerName(Class<?> clazz) {
        java.util.Objects.requireNonNull(clazz, "Controller类不能为空");
        Controller controllerAnnon = clazz.getAnnotation(Controller.class);
        String className = clazz.getSimpleName();
        if(null == controllerAnnon && className.endsWith(CONTROLLER_FIELD)) {
            className = className.substring(0, CONTROLLER_FIELD.length());
        } else {
            className = controllerAnnon.value();
        }
        return className;
    }

}
