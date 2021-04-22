package cn.leancloud.session;

import cn.leancloud.LCLogger;
import cn.leancloud.utils.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LCSessionManager {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCSessionManager.class);
  private static LCSessionManager instance = null;

  private final Map<String, LCSession> peerIdEnabledSessions = Collections
          .synchronizedMap(new HashMap<String, LCSession>());

  public static LCSessionManager getInstance() {
    if (null == instance) {
      synchronized (LCSessionManager.class) {
        if (null == instance) {
          instance = new LCSessionManager();
        }
      }
    }
    return instance;
  }

  private LCSessionManager() {
    initSessionsIfExists();
  }

  private void initSessionsIfExists() {
//    Map<String, String> cachedSessions = AVSessionCacheHelper.getTagCacheInstance().getAllSession();
//    for (Map.Entry<String, String> entry : cachedSessions.entrySet()) {
//      AVSession s = getOrCreateSession(entry.getKey());
//      s.setSessionResume(true);
//      s.setTag(entry.getValue());
//    }
  }

  public LCSession getOrCreateSession(String peerId, String installationId, LCConnectionManager connectionManager) {
    try {
      // 据说这行有NPE，所以不得不catch起来避免app崩溃
      boolean newAdded = !peerIdEnabledSessions.containsKey(peerId);
      LCSession session = null;
      if (newAdded) {
        session = new LCSession(connectionManager, peerId, installationId, new DefaultSessionListener());
        connectionManager.subscribeConnectionListener(peerId, session.getWebSocketListener());
        peerIdEnabledSessions.put(peerId, session);
      } else {
        session = peerIdEnabledSessions.get(peerId);
      }
      return session;
    } catch (Exception e) {
      LOGGER.w("failed to create Session instance.", e);
      return null;
    }
  }

  public void removeSession(String peerId) {
    LCSession session = peerIdEnabledSessions.remove(peerId);
    if (session != null && session.getWebSocketListener() != null) {
      session.connectionManager.unsubscribeConnectionListener(session.getSelfPeerId());
    }
  }

}
