package cn.leancloud.push.lite.ws;

import java.nio.ByteBuffer;

public interface WebSocketClientMonitor {
  void onOpen();
  void onClose(int var1, String var2, boolean var3);
  void onMessage(ByteBuffer bytes);
  void onError(Exception exception);
}
