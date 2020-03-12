package cn.leancloud.session;

import cn.leancloud.im.AVIMOptions;
import cn.leancloud.util.WeakConcurrentHashMap;

import java.util.List;

public class RequestStormSuppression {
  private static volatile RequestStormSuppression _instance = null;
  interface RequestCallback {
    void done(AVIMOperationQueue.Operation operation);
  }

//  ConcurrentMap<String, List<AVIMOperationQueue.Operation>> operations = new ConcurrentHashMap<>();
  WeakConcurrentHashMap<String, AVIMOperationQueue.Operation> operations = null;

  public static RequestStormSuppression getInstance() {
    if (null == _instance) {
      synchronized (RequestStormSuppression.class) {
        if (null == _instance) {
          _instance = new RequestStormSuppression();
        }
      }
    }
    return _instance;
  }

  private RequestStormSuppression() {
    long expiryInMillis = AVIMOptions.getGlobalOptions().getTimeoutInSecs() * 1000;
    if (expiryInMillis < 1000) {
      expiryInMillis = 10000;
    }
    operations = new WeakConcurrentHashMap<>(expiryInMillis);
  }

  String getCacheKey(AVIMOperationQueue.Operation operation) {
    return String.format("%s/%d/%s", operation.sessionId, operation.operation, operation.identifier);
  }

  public boolean postpone(AVIMOperationQueue.Operation operation) {
    if (null == operation) {
      return false;
    }
    String cachedKey = getCacheKey(operation);
    boolean found = false;
    synchronized (this) {
      found = operations.containsKey(cachedKey);
      operations.addElement(cachedKey, operation);
    }
    return found;
  }

  public synchronized int getCacheSize() {
    return operations.size();
  }

  public synchronized void cleanup() {
    operations.clear();
  }

  public void release(AVIMOperationQueue.Operation operation, RequestCallback callback) {
    if (null == operation) {
      return;
    }
    String cachedKey = getCacheKey(operation);
    List<AVIMOperationQueue.Operation> cachedOperations = null;
    synchronized (this) {
      if (operations.containsKey(cachedKey)) {
        cachedOperations = operations.remove(cachedKey);
      }
    }
    if (null != cachedOperations && null != callback) {
      for (AVIMOperationQueue.Operation op : cachedOperations) {
        callback.done(op);
      }
    }
  }
}
