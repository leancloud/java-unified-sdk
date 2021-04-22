package cn.leancloud.im;

import cn.leancloud.im.v2.LCIMClient;
import cn.leancloud.im.v2.LCIMClientEventHandler;

public class DummyClientEventHandler extends LCIMClientEventHandler {
  public void onConnectionPaused(LCIMClient client) {
    System.out.println("onConnectionPaused. client=" + client.getClientId());
  }

  /**
   * 实现本方法以处理网络恢复事件
   *
   * @since 3.0
   * @param client
   */

  public void onConnectionResume(LCIMClient client) {
    System.out.println("onConnectionResume. client=" + client.getClientId());
  }

  /**
   * 实现本方法以处理当前登录被踢下线的情况
   *
   *
   * @param client
   * @param code 状态码说明被踢下线的具体原因
   */

  public void onClientOffline(LCIMClient client, int code) {
    System.out.println("onClientOffline. client=" + client.getClientId() + ", code=" + code);
  }

}
