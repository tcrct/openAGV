package com.makerwit.core.component;

import com.openagv.mvc.core.interfaces.IComponents;
import com.openagv.mvc.core.interfaces.IProtocolDecode;
import com.openagv.mvc.core.interfaces.IRepeatSend;

/**
 * OpenAgv组件
 * 每个组件默认使用单例
 *
 * @author Laotang
 */
public class OpenAgvComponent implements IComponents {
    @Override
    public IProtocolDecode getProtocolDecode() {
        return ProtocolDecode.duang();
    }

    @Override
    public IRepeatSend getRepeatSend() {
        return RepeatSend.duang();
    }
}
