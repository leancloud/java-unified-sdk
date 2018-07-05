package cn.leancloud.im.v2;

import cn.leancloud.AVLogger;
import cn.leancloud.im.AVIMEventHandler;
import cn.leancloud.utils.LogUtil;

/**
 *
 * 用于处理Client相关的事件，包括网络连接断开和网络连接恢复
 */
public abstract class AVIMClientEventHandler extends AVIMEventHandler {
  protected static final AVLogger LOGGER = LogUtil.getLogger(AVIMClientEventHandler.class);

  /**
   * 实现本方法以处理网络断开事件
   *
   * @param client
   * @since 3.0
   */
  public abstract void onConnectionPaused(AVIMClient client);

  /**
   * 实现本方法以处理网络恢复事件
   *
   * @since 3.0
   * @param client
   */

  public abstract void onConnectionResume(AVIMClient client);

  /**
   * 实现本方法以处理当前登录被踢下线的情况
   *
   *
   * @param client
   * @param code 状态码说明被踢下线的具体原因
   */

  public abstract void onClientOffline(AVIMClient client, int code);

  @Override
  protected final void processEvent0(int operation, Object operator, Object operand,
                                     Object eventScene) {
    switch (operation) {
      case Conversation.STATUS_ON_CONNECTION_RESUMED:
        onConnectionResume((AVIMClient) eventScene);
        break;
      case Conversation.STATUS_ON_CONNECTION_PAUSED:
        onConnectionPaused((AVIMClient) eventScene);
        break;
      case Conversation.STATUS_ON_CLIENT_OFFLINE:
        onClientOffline((AVIMClient) eventScene, (Integer) operand);
        ((AVIMClient) eventScene).close();
        break;
      default:
        LOGGER.d("Not supported operation:" + operand);
    }
  }

}