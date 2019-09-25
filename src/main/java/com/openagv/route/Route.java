package com.openagv.route;

public class Route {

    private String key;
    /**执行的控制器类*/
    private Object injectController;

    public Route(String key, Object injectController) {
        this.key = key;
        this.injectController = injectController;
    }

    public String getKey() {
        return key;
    }

    public Object getInjectController() {
        return injectController;
    }

    @Override
    public String toString() {
        return "Route{" +
                "key='" + key + '\'' +
                ", injectController=" + injectController.getClass().getName() +
                '}';
    }
}
