package com.robot.utils;

import com.robot.mvc.core.exceptions.RobotException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 传递临时值工具
 * 用于传递临时值用
 *
 * @author Laotang
 */
public class TempValueKit {

    private static final Map<String, Object> TEMP_VALUE_MAP = new ConcurrentHashMap<>();
    private static TempValueKit TEMP_VALUE_UTIL = new TempValueKit();

    public static TempValueKit duang() {
        return TEMP_VALUE_UTIL;
    }

    private String key;
    private Object value;

    public TempValueKit key(String key) {
        this.key = key;
        return this;
    }

    public TempValueKit value(Object value) {
        this.value = value;
        return this;
    }

    public void set() {
        if (ToolsKit.isEmpty(key)) {
            throw new RobotException("设置临时值时，[key]关键字不能为空");
        }
        if (ToolsKit.isEmpty(value)) {
            throw new RobotException("设置临时值时，[value]值不能为空");
        }
        TEMP_VALUE_MAP.put(key, value);
    }

    public <T> T get() {
        if (ToolsKit.isEmpty(key)) {
            throw new RobotException("取临时值时，[key]关键字不能为空");
        }
        return (T)TEMP_VALUE_MAP.get(key);
    }

    public void remove() {
        if (ToolsKit.isEmpty(key)) {
            throw new RobotException("取临时值时，[key]关键字不能为空");
        }
        TEMP_VALUE_MAP.remove(key);
    }

}
