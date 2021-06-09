package cn.leancloud.session;

import cn.leancloud.im.LCIMOptions;
import cn.leancloud.util.WeakConcurrentHashMap;

import java.util.List;

public class RequestStormSuppression {
  private static volatile RequestStormSuppression _instance = null;
  interface RequestCallback {
    void done(IMOperationQueue.Operation operation);
  }

  WeakConcurrentHashMap<String, IMOperationQueue.Operation> operations = null;

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
    long expiryInMillis = LCIMOptions.getGlobalOptions().getTimeoutInSecs() * 1000;
    if (expiryInMillis < 1000) {
      expiryInMillis = 10000;
    }
    operations = new WeakConcurrentHashMap<>(expiryInMillis);
  }

  String getCacheKey(IMOperationQueue.Operation operation) {
    return String.format("%s/%d/%s", operation.sessionId, operation.operation, operation.identifier);
  }

  public boolean postpone(IMOperationQueue.Operation operation) {
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

  public void release(IMOperationQueue.Operation operation, RequestCallback callback) {
    if (null == operation) {
      return;
    }
    String cachedKey = getCacheKey(operation);
    List<IMOperationQueue.Operation> cachedOperations = null;
    synchronized (this) {
      if (operations.containsKey(cachedKey)) {
        cachedOperations = operations.remove(cachedKey);
      }
    }
    if (null != cachedOperations && null != callback) {
      for (IMOperationQueue.Operation op : cachedOperations) {
        callback.done(op);
      }
    }
  }
}
