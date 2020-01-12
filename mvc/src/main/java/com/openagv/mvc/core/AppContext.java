package com.openagv.mvc.core;

import com.openagv.mvc.core.interfaces.IHandler;
import com.openagv.mvc.utils.SettingUtils;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laotang on 2020/1/12.
 */
public class AppContext {

    private static final List<IHandler> BEFORE_HEANDLER_LIST = new ArrayList<>();
    private static Boolean isAnswer = null;

    /***
     * 取前置处理器
     * @return
     */
    public static List<IHandler> getBeforeHeandlerList() {
        return BEFORE_HEANDLER_LIST;
    }


    /**是否需要进行握手回复*/
    public static boolean isAnswer() {
        if (null == isAnswer) {
            isAnswer = SettingUtils.getBoolean("answer", true);
        }
        return isAnswer;
    }
}


