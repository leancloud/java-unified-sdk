package cn.leancloud.session;

import cn.leancloud.command.CommandPacket;

import java.util.List;

public abstract class AVSessionListener {

  public abstract void onError(AVSession session, Throwable e, int sessionOperation,
                               int requestId);

  public void onError(AVSession session, Throwable e) {
    this.onError(session, e, AVSession.OPERATION_UNKNOW, CommandPacket.UNSUPPORTED_OPERATION);
  }

  public abstract void onSessionOpen(AVSession session, int requestId);

  public abstract void onSessionClose(AVSession session, int requestId);

  public abstract void onSessionTokenRenewed(AVSession session, int requestId);

  public abstract void onSessionPaused(AVSession session);

  public abstract void onSessionResumed(AVSession session);

  public abstract void onOnlineQuery(AVSession session, List<String> onlinePeerIds,
                                     int requestCode);

  /*
   * 这个方法主要是用来处理服务器端的主动登出当前用户的登录的
   */
  public abstract void onSessionClosedFromServer(AVSession session, int code);

}

