package cn.leancloud.im;

public class AVIMOptions {
  private static final AVIMOptions globalOptions = new AVIMOptions();
  private String rtmConnectionServer = "";
  private SignatureFactory signatureFactory = null;
  private int timeoutInSecs = 10;
  private boolean isAutoOpen = true;
  private boolean messageQueryCacheEnabled = false;

  private boolean wrapMessageBinaryBufferAsString = false;

  private SystemReporter systemReporter = null;

  /**
   * 在 session/open 的时候是否总是获取全部未读消息通知
   */
  private boolean alwaysRetrieveAllNotification = false;

  /**
   * 停止推送的自动 login 请求（针对不使用推送的应用）
   */
  private boolean disableAutoLogin4Push = false;

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

  public boolean isAlwaysRetrieveAllNotification() {
    return alwaysRetrieveAllNotification;
  }

  /**
   * 在 session/open 的时候是否总是获取全部未读消息通知
   * 1，如果不是每次都获取全部的未读消息通知（默认方式），那么应用层需要缓存之前收到的通知数据；
   * 2，如果强制每次都获取全部的未读消息通知，由于 LeanCloud 即时通讯云端最多缓存 50 个对话的未读消息通知，所以应用层也还是应该缓存之前收到
   * 的通知数据（如果业务层面保证一个人不会拥有超过 50 个对话，则可以不必缓存）。
   *
   * @param alwaysRetrieveAllNotification 强制总是获取
   */
  public void setAlwaysRetrieveAllNotification(boolean alwaysRetrieveAllNotification) {
    this.alwaysRetrieveAllNotification = alwaysRetrieveAllNotification;
  }

  public boolean isDisableAutoLogin4Push() {
    return disableAutoLogin4Push;
  }

  /**
   * 设置是否禁止推送服务的自动 login 请求
   * 对于部分应用来说，如果不使用 LeanCloud 推送服务，仅仅只使用了即时通讯服务的话，可以将这个标志设为 true，以避免不必要的网络连接。
   *
   * @param disableAutoLogin4Push disable flag
   */
  public void setDisableAutoLogin4Push(boolean disableAutoLogin4Push) {
    this.disableAutoLogin4Push = disableAutoLogin4Push;
  }

  public SystemReporter getSystemReporter() {
    return systemReporter;
  }

  public void setSystemReporter(SystemReporter reporter) {
    this.systemReporter = reporter;
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
