package cn.leancloud.im;

import cn.leancloud.callback.AVCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestCache {
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
