package cn.leancloud.session;

import cn.leancloud.AVLogger;
import cn.leancloud.utils.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AVSessionManager {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVSessionManager.class);
  private static AVSessionManager instance = null;

  private final Map<String, AVSession> peerIdEnabledSessions = Collections
          .synchronizedMap(new HashMap<String, AVSession>());

  public static AVSessionManager getInstance() {
    if (null == instance) {
      synchronized (AVSessionManager.class) {
        if (null == instance) {
          instance = new AVSessionManager();
        }
      }
    }
    return instance;
  }

  private AVSessionManager() {
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

  public AVSession getOrCreateSession(String peerId, String installationId, AVConnectionManager connectionManager) {
    try {
      // 据说这行有NPE，所以不得不catch起来避免app崩溃
      boolean newAdded = !peerIdEnabledSessions.containsKey(peerId);
      AVSession session = null;
      if (newAdded) {
        session = new AVSession(connectionManager, peerId, installationId, new AVDefaultSessionListener());
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
    AVSession session = peerIdEnabledSessions.remove(peerId);
    if (session != null && session.getWebSocketListener() != null) {
      session.connectionManager.unsubscribeConnectionListener(session.getSelfPeerId());
    }
  }

}
