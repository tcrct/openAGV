package com.robot.event.core;

import java.util.EventObject;

/**
 * 事件传送对象
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-30
 */
public class Event extends EventObject {

	private static final long serialVersionUID = 8103209835359171288L;
	
	private final long currentTime;
	
	public Event(Object source) {
		super(source);
		currentTime = System.currentTimeMillis();
	}

	public final long getCurrenttime() {
		return currentTime;
	}
}
