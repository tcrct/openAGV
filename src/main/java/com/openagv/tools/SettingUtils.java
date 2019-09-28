package com.openagv.tools;

import cn.hutool.setting.Setting;

public class SettingUtils {

    private static final String SETTING_FILE_NAME = "app.setting";

    private static Setting SETTING = new Setting(SETTING_FILE_NAME);

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return SETTING.getStr(key, defaultValue);
    }

    public static String getStringByGroup(String key, String group, String defaultValue) {
        return SETTING.getStr(key, group, defaultValue);
    }

    public static int getInt(String key, int defaultValue){
        return SETTING.getInt(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        return SETTING.getBool(key, defaultValue);
    }


}
