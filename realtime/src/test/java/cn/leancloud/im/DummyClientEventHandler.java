package cn.leancloud.im;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;

public class DummyClientEventHandler extends AVIMClientEventHandler {
  public void onConnectionPaused(AVIMClient client) {
    System.out.println("onConnectionPaused. client=" + client.getClientId());
  }

  /**
   * 实现本方法以处理网络恢复事件
   *
   * @since 3.0
   * @param client
   */

  public void onConnectionResume(AVIMClient client) {
    System.out.println("onConnectionResume. client=" + client.getClientId());
  }

  /**
   * 实现本方法以处理当前登录被踢下线的情况
   *
   *
   * @param client
   * @param code 状态码说明被踢下线的具体原因
   */

  public void onClientOffline(AVIMClient client, int code) {
    System.out.println("onClientOffline. client=" + client.getClientId() + ", code=" + code);
  }

}
