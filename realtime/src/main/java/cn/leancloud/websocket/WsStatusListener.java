package cn.leancloud.websocket;

import okhttp3.Response;
import okio.ByteString;

public interface WsStatusListener {
  void onOpen(Response response);
  void onMessage(String text);
  void onMessage(ByteString bytes);
  void onReconnect();
  void onClosing(int code, String reason);
  void onClosed(int code, String reason);
  void onFailure(Throwable t, Response response);
}
