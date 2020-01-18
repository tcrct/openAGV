package com.openagv.mvc.core.telegram;

/**
 * 发送电报接口
 *
 * @author Laotang
 */
public interface ITelegramSender {

  /**
   * 发送报文
   *
   * @param request The {@link Request} to be sent.
   */
  void sendTelegram(ITelegram telegram);
}
