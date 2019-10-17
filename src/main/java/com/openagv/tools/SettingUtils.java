package com.openagv.tools;

import cn.hutool.setting.Setting;

import java.util.*;

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

    public static int getInt(String key, String group, int defaultValue){
        return SETTING.getInt(key, group, defaultValue);
    }


    public static boolean getBoolean(String key, boolean defaultValue){
        return SETTING.getBool(key, defaultValue);
    }

    public static boolean getBoolean(String key, String group, boolean defaultValue){
        return SETTING.getBool(key,group, defaultValue);
    }

    public static String[] getStringArray(String key){
        return SETTING.getStrings(key);
    }

    public static String[] getStringArray(String key, String group){
        return SETTING.getStrings(key,group);
    }

    public static List<String> getStringList(String key){
        String[] strings = SETTING.getStrings(key);
        if(ToolsKit.isEmpty(strings)) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>(strings.length);
        for(String str : strings) {
            list.add(str);
        }
        return list;
    }

    public static Set<String> getStringsToSet(String key,String group) {
        String[] stringArray = getStringArray(key, group);
        Set<String> set = new HashSet<>();
        if(ToolsKit.isEmpty(stringArray)) {
            return set;
        }
        for(String str : stringArray) {
            set.add(str);
        }
        return set;
    }


}
