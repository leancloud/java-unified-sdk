package cn.leancloud.im;

import cn.leancloud.AVLogger;
import cn.leancloud.callback.AVCallback;
import cn.leancloud.utils.LogUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCache {
  private static final AVLogger LOGGER = LogUtil.getLogger(RequestCache.class);

  public static final RequestCache instance = new RequestCache();
  private static final String KEY_FORMAT = "%s/%s/%d";
  private Map<String, AVCallback> requests = new ConcurrentHashMap<>();
  private RequestCache() {
  }

  public static RequestCache getInstance() {
    return instance;
  }

  public void addRequestCallback(String clientId, String conversationId, int requestId, AVCallback callback) {
    String cacheKey = getCacheKey(clientId, conversationId, requestId);
    this.requests.put(cacheKey, callback);
    LOGGER.d("add request cache. client=" + clientId + ", conv=" + conversationId + ", request=" + requestId);
  }

  public AVCallback getRequestCallback(String clientId, String conversationId, int requestId) {
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
