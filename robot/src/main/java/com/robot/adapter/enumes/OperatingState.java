package com.robot.adapter.enumes;

/**
 * 车辆状态
 *
 * @author Laotang
 */
public enum OperatingState {

    /**
     * 车辆当前正在执行操作
     */
    ACTING,
    /**
     * 车辆当前处于空闲状态.
     */
    IDLE,
    /**
     * 车辆当前处于移动状态.
     */
    MOVING,
    /**
     * 车辆当前处于错误状态.
     */
    ERROR,
    /**
     * 车辆正在充电.
     */
    CHARGING,
    /**
     * 车辆状态目前未知.
     */
    UNKNOWN
}
