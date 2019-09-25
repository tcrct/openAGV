package com.openagv.route;

public class Route {

    private String key;
    /**执行的控制器类*/
    private Object injectObj;

    public Route(String key, Object injectObj) {
        this.key = key;
        this.injectObj = injectObj;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key='" + key + '\'' +
                ", injectObj=" + injectObj.getClass().getName() +
                '}';
    }
}
