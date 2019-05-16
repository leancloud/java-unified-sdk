package cn.leancloud.push.lite.ws;

import cn.leancloud.push.lite.proto.Messages;

public interface AVConnectionListener {
  void onWebSocketOpen();
  void onWebSocketClose();
  void onMessageArriving(String peerId, Integer requestKey, Messages.GenericCommand genericCommand);
  void onError(Integer requestKey, Messages.ErrorCommand errorCommand);
}
