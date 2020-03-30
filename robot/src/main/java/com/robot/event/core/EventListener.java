package com.robot.event.core;

/**
 * 事件监听器
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-30
 */
public interface EventListener<T> extends java.util.EventListener {
	T onEvent(Event event);
}
