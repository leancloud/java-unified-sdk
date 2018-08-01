package cn.leancloud.session;

import cn.leancloud.utils.StringUtil;

public class StaleMessageDepot {
  PersistentQueue<String> messageDepot;
  private static final int MAXLENGTH = 50;

  public StaleMessageDepot(String depotName) {
    this.messageDepot = new PersistentQueue<String>(depotName, String.class);
  }

  /**
   *
   * @param messageId
   * @return false if message arrived before. true
   */
  public synchronized boolean putStableMessage(String messageId) {
    if (StringUtil.isEmpty(messageId)) {
      return true;
    }
    boolean isContains = messageDepot.contains(messageId);
    if (!isContains) {
      messageDepot.offer(messageId);
      while (messageDepot.size() > MAXLENGTH) {
        messageDepot.poll();
      }
    }
    return !isContains;
  }
}
