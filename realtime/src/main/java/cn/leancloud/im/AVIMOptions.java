package cn.leancloud.im;

public class AVIMOptions {
  private static final AVIMOptions globalOptions = new AVIMOptions();
  private String rtmConnectionServer = "";
  private SignatureFactory signatureFactory = null;
  private int timeoutInSecs = 10;
  private boolean isAutoOpen = true;
  private boolean messageQueryCacheEnabled = false;

  private boolean wrapMessageBinaryBufferAsString = false;

  /**
   * 离线消息推送模式
   * true 为仅推送数量，false 为推送具体消息
   */
  private boolean onlyPushCount = false;

  /**
   * get global options instance.
   * @return Returns current object, so you can chain this call.
   */
  public static AVIMOptions getGlobalOptions() {
    return globalOptions;
  }

  /**
   * set rtm connection server.
   * @param server rtm connection server.
   */
  public void setRtmServer(String server) {
    this.rtmConnectionServer = server;
  }

  /**
   * get rtm connection server.
   * @return rtm connection server.
   */
  public String getRtmServer() {
    return this.rtmConnectionServer;
  }

  /**
   * get signature factory
   * @return signature factory.
   */
  public SignatureFactory getSignatureFactory() {
    return this.signatureFactory;
  }

  /**
   * set signature factory.
   * @param factory signature factory.
   */
  public void setSignatureFactory(SignatureFactory factory) {
    this.signatureFactory = factory;
  }

  /**
   * get timeout option.
   * @return timeout value(seconds)
   */
  public int getTimeoutInSecs() {
    return timeoutInSecs;
  }

  /**
   * set timeout option.
   * @param timeoutInSecs timeout value
   */
  public void setTimeoutInSecs(int timeoutInSecs) {
    this.timeoutInSecs = timeoutInSecs;
  }

  public boolean isWrapMessageBinaryBufferAsString() {
    return this.wrapMessageBinaryBufferAsString;
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
   * @param isOnlyCount flag indicates only push count or not
   */
  public void setUnreadNotificationEnabled(boolean isOnlyCount) {
    onlyPushCount = isOnlyCount;
  }

  /**
   * 是否被设置为离线消息仅推送数量
   * @return flag indicates only push count or not.
   */
  public boolean isOnlyPushCount() {
    return onlyPushCount;
  }

  /**
   * 是否被设置为即时重置网络连接
   * @return flag indicates reset connection or not.
   *
   * @deprecated Since 5.0.18
   */
  public boolean isResetConnectionWhileBroken() {
    return true;
  }

  /**
   * 设置为即时重置网络连接
   * @param resetConnectionWhileBroken flag indicating reset connection while broken or not.
   *
   * @deprecated Since 5.0.18
   */
  public void setResetConnectionWhileBroken(boolean resetConnectionWhileBroken) {
  }

  private AVIMOptions() {
  }
}
