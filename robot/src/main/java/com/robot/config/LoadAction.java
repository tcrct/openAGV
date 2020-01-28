/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.config;

import org.opentcs.data.order.DriveOrder;

/**
 * 定义（可配置）用于加载和卸载的字符串，可用于内核模型中的车辆操作。
 *
 * @author Laotang
 */
public interface LoadAction {

  public static final String NONE = DriveOrder.Destination.OP_NOP;
  /**
   * 装货
   */
  public static final String LOAD = "Load cargo";
  /**
   *卸货
   */
  public static final String UNLOAD = "Unload cargo";
  /**
   * 充电
   */
  public static final String CHARGE = "Charge";
}
