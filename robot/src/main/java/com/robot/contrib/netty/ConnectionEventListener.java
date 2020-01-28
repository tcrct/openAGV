package com.robot.contrib.netty;

/**
 * A listener for events concerning an established connection.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @param <T> The type of the incoming messages handled by this listener.
 */
public interface ConnectionEventListener<T> {

  /**
   * Called when a message from the remote peer has been received and decoded.
   *
   * @param telegram The incoming message.
   */
  void onIncomingTelegram(T telegram);

  /**
   * Called when a connection to the remote peer has been established.
   */
  void onConnect();

  /**
   * Called when a connnection attempt to the remote peer has failed.
   */
  void onFailedConnectionAttempt();

  /**
   * Called when a connection to the remote peer has been closed (by either side).
   */
  void onDisconnect();

  /**
   * Called when the remote peer is considered idle.
   */
  void onIdle();
}
