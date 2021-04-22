package cn.leancloud.session;

import cn.leancloud.command.CommandPacket;

import java.util.List;

public abstract class LCSessionListener {

  public abstract void onError(LCSession session, Throwable e, int sessionOperation,
                               int requestId);

  public void onError(LCSession session, Throwable e) {
    this.onError(session, e, LCSession.OPERATION_UNKNOW, CommandPacket.UNSUPPORTED_OPERATION);
  }

  public abstract void onSessionOpen(LCSession session, int requestId);

  public abstract void onSessionClose(LCSession session, int requestId);

  public abstract void onSessionTokenRenewed(LCSession session, int requestId);

  public abstract void onSessionPaused(LCSession session);

  public abstract void onSessionResumed(LCSession session);

  public abstract void onOnlineQuery(LCSession session, List<String> onlinePeerIds,
                                     int requestCode);

  /*
   * 这个方法主要是用来处理服务器端的主动登出当前用户的登录的
   */
  public abstract void onSessionClosedFromServer(LCSession session, int code);

}

