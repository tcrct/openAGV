package com.makerwit.core.component;


import com.robot.mvc.core.interfaces.IComponents;
import com.robot.mvc.core.interfaces.IProtocolMatcher;
import com.robot.mvc.core.interfaces.IRepeatSend;

/**
 * OpenAgv组件
 * 每个组件默认使用单例
 *
 * @author Laotang
 */
public class RobotComponent implements IComponents {
    @Override
    public IProtocolMatcher getProtocolMatcher() {
        return ProtocolMatcher.duang();
    }

    @Override
    public IRepeatSend getRepeatSend() {
        return RepeatSend.duang();
    }
}
