package cn.leancloud.im;

import cn.leancloud.LCLogger;
import cn.leancloud.callback.LCCallback;
import cn.leancloud.utils.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCache {
  private static final LCLogger LOGGER = LogUtil.getLogger(RequestCache.class);

  public static final RequestCache instance = new RequestCache();
  private static final String KEY_FORMAT = "%s/%s/%d";
  private Map<String, LCCallback> requests = new ConcurrentHashMap<>();
  private RequestCache() {
  }

  public static RequestCache getInstance() {
    return instance;
  }

  public void addRequestCallback(String clientId, String conversationId, int requestId, LCCallback callback) {
    String cacheKey = getCacheKey(clientId, conversationId, requestId);
    this.requests.put(cacheKey, callback);
    LOGGER.d("add request cache. client=" + clientId + ", conv=" + conversationId + ", request=" + requestId);
  }

  public LCCallback getRequestCallback(String clientId, String conversationId, int requestId) {
    String cacheKey = getCacheKey(clientId, conversationId, requestId);
    return this.requests.get(cacheKey);
  }

  public void cleanRequestCallback(String clientId, String conversationId, int requestId) {
    String cacheKey = getCacheKey(clientId, conversationId, requestId);
    this.requests.remove(cacheKey);
  }

  private String getCacheKey(String clientId, String conversationId, int requestId) {
    return String.format(KEY_FORMAT, clientId, conversationId, requestId);
  }
}
