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

}
