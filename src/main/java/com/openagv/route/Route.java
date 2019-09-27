package com.openagv.route;

public class Route {

    private String key;
    /**执行的控制器类*/
    private Object injectObject;

    public Route(String key, Object injectObject) {
        this.key = key;
        this.injectObject = injectObject;
    }

    public String getKey() {
        return key;
    }

    public Object getInjectObject() {
        return injectObject;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key='" + key + '\'' +
                ", Class=" + injectObject.getClass().getName() +
                '}';
    }
}
