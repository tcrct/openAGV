/**
 * Copyright (c) Fraunhofer IML
 */
package com.openagv.opentcs.telegrams;

import com.google.inject.assistedinject.Assisted;
import com.openagv.tools.SettingUtils;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionListener;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public class StateRequesterTask {

  /**
   * This class's logger.
   */
  private static final Logger LOG = Logger.getLogger(StateRequesterTask.class);
  /**
   * The actual action to be performed to enqueue requests.
   */
  private final ActionListener stateRequestAction;
  /**
   * A timer for enqueuing requests.
   */
  private Timer stateRequestTimer;
  /**
   * 间隔时间，默认为1秒
   */
  private int requestInterval = 1000;

  /**
   * Creates a new instance.
   *
   * @param stateRequestAction The actual action to be performed to enqueue requests.
   */
  @Inject
  public StateRequesterTask(@Nonnull @Assisted ActionListener stateRequestAction) {
    this.stateRequestAction = requireNonNull(stateRequestAction, "stateRequestAction");
  }

  public void enable() {
    if (stateRequestTimer != null) {
      return;
    }
    LOG.info("Starting state requester task.");
    stateRequestTimer = new Timer(getRequestInterval(), stateRequestAction);
    stateRequestTimer.start();
  }

  public void disable() {
    if (stateRequestTimer == null) {
      return;
    }
    LOG.info("Stopping state requester task.");
    stateRequestTimer.stop();
    stateRequestTimer = null;
  }

  /**
   * Restarts the timer for enqueuing new requests.
   */
  public void restart() {
    if (stateRequestTimer == null) {
      LOG.info("Not enabled, doing nothing.");
      return;
    }
    stateRequestTimer.restart();
  }

  /**
   * 设置定时器的时间间隔
   * */
  public int getRequestInterval() {
    return SettingUtils.getInt("handshake.interval", "adapter", requestInterval);
  }

}
