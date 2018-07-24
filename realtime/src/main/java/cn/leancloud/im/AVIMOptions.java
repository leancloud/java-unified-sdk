package cn.leancloud.im;

public class AVIMOptions {
  private static final AVIMOptions globalOptions = new AVIMOptions();
  private String rtmConnectionServer = "";
  private SignatureFactory signatureFactory = null;
  private int timeoutInSecs = 10;
  private boolean isAutoOpen = true;
  private boolean messageQueryCacheEnabled = true;
  /**
   * 离线消息推送模式
   * true 为仅推送数量，false 为推送具体消息
   */
  private boolean onlyPushCount = false;

  /**
   * get global options instance.
   * @return
   */
  public static AVIMOptions getGlobalOptions() {
    return globalOptions;
  }

  /**
   * set rtm connection server.
   * @param server
   */
  public void setRtmServer(String server) {
    this.rtmConnectionServer = server;
  }

  /**
   * get rtm connection server.
   * @return
   */
  public String getRtmServer() {
    return this.rtmConnectionServer;
  }

  /**
   * get signature factory
   * @return
   */
  public SignatureFactory getSignatureFactory() {
    return this.signatureFactory;
  }

  /**
   * set signature factory.
   * @param factory
   */
  public void setSignatureFactory(SignatureFactory factory) {
    this.signatureFactory = factory;
  }

  /**
   * get timeout option.
   * @return
   */
  public int getTimeoutInSecs() {
    return timeoutInSecs;
  }

  /**
   * set timeout option.
   * @param timeoutInSecs
   */
  public void setTimeoutInSecs(int timeoutInSecs) {
    this.timeoutInSecs = timeoutInSecs;
  }

  public boolean isAutoOpen() {
    return isAutoOpen;
  }

  public void setAutoOpen(boolean autoOpen) {
    isAutoOpen = autoOpen;
  }

  public boolean isMessageQueryCacheEnabled() {
    return messageQueryCacheEnabled;
  }

  public void setMessageQueryCacheEnabled(boolean messageQueryCacheEnabled) {
    this.messageQueryCacheEnabled = messageQueryCacheEnabled;
  }
  /**
   * 设置离线消息推送模式
   * @param isOnlyCount
   */
  public void setUnreadNotificationEnabled(boolean isOnlyCount) {
    onlyPushCount = isOnlyCount;
  }

  /**
   * 是否被设置为离线消息仅推送数量
   * @return
   */
  public boolean isOnlyPushCount() {
    return onlyPushCount;
  }

  private AVIMOptions() {
  }
}
