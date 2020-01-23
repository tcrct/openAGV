package com.openagv.config;

/**
 * 车辆的负载状态
 *
 * @author Laotang
 */
public enum LoadState {

    /**
     * 当前车辆的负载处理状态为空
     */
    EMPTY,
    /**
     * 当前车辆的负载处理状态为已满
     */
    FULL,
    /**
     * 当前车辆的负载处理状态为未知
     */
    UNKNOWN

}
