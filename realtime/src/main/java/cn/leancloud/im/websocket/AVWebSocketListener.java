package cn.leancloud.im.websocket;

import cn.leancloud.im.Messages;

public interface AVWebSocketListener {
  void onWebSocketOpen();

  void onWebSocketClose();

  void onDirectCommand(Messages.DirectCommand directCommand);

  void onSessionCommand(String op, Integer requestId, Messages.SessionCommand command);

  void onAckCommand(Integer requestKey, Messages.AckCommand ackCommand);

  void onMessageReceipt(Messages.RcpCommand rcpCommand);

  void onReadCmdReceipt(Messages.RcpCommand rcpCommand);

  void onListenerAdded(boolean open);

  void onListenerRemoved();

  void onBlacklistCommand(String operation, Integer requestKey, Messages.BlacklistCommand blacklistCommand);

  void onConversationCommand(String operation, Integer requestKey, Messages.ConvCommand convCommand);

  void onError(Integer requestKey, Messages.ErrorCommand errorCommand);

  void onHistoryMessageQuery(Integer requestKey, Messages.LogsCommand command);

  /**
   * process unread count of offline messages
   */
  void onUnreadMessagesCommand(Messages.UnreadCommand unreadCommand);

  void onMessagePatchCommand(boolean isModify, Integer requestKey, Messages.PatchCommand command);
}
