package cn.leancloud.session;

import cn.leancloud.Messages;

public interface LCConnectionListener {
  void onWebSocketOpen();

  void onWebSocketClose();

  void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand genericCommand);

  void onError(Integer requestKey, Messages.ErrorCommand errorCommand);
}
