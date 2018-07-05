package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageOption;
import cn.leancloud.im.v2.callback.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageBridge {
  private static final MessageBridge gInstance = new MessageBridge();
  private static AtomicInteger acu = new AtomicInteger(-65536);
  private MessageBridge() {}

  public static MessageBridge getInstance() {
    return MessageBridge.gInstance;
  }

  public static int getNextRequestId() {
    int val = acu.incrementAndGet();
    if (val > 65535) {
      while (val > 65535 && !acu.compareAndSet(val, -65536)) {
        val = acu.get();
      }
      return val;
    } else {
      return val;
    }
  }

  public void openClient(String clientId, String tag, String userSessionToken,
                         boolean reConnect, AVIMClientCallback callback) {
    ;
  }

  public void queryClientStatus(String clientId, final AVIMClientStatusCallback callback) {
    ;
  }

  public void closeClient(String self, AVIMClientCallback callback) {
    ;
  }

  public void queryOnlineClients(String self, List<String> clients, final AVIMOnlineClientsCallback callback) {
    ;
  }

  public void createConversation(final List<String> members, final String name,
                                 final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
                                 final boolean isTemp, int tempTTL, final AVIMConversationCreatedCallback callback) {
    ;
  }

  public void sendMessage(final AVIMMessage message, final AVIMMessageOption messageOption, final AVIMConversationCallback callback) {
    ;
  }

  public void updateMessage(AVIMMessage oldMessage, AVIMMessage newMessage, AVIMMessageUpdatedCallback callback) {
    ;
  }

  public void recallMessage(AVIMMessage message, AVIMMessageRecalledCallback callback) {
    ;
  }
}
