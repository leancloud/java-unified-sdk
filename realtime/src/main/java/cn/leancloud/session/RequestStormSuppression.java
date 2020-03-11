package cn.leancloud.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RequestStormSuppression {
  private static final RequestStormSuppression instance = new RequestStormSuppression();
  interface RequestCallback {
    void done(AVIMOperationQueue.Operation operation);
  }

  ConcurrentMap<String, List<AVIMOperationQueue.Operation>> operations = new ConcurrentHashMap<>();

  public static RequestStormSuppression getInstance() {
    return instance;
  }

  private RequestStormSuppression() {
    ;
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
      if (found) {
        operations.get(cachedKey).add(operation);
      } else {
        List<AVIMOperationQueue.Operation> array = new ArrayList<>();
        array.add(operation);
        operations.put(cachedKey, array);
      }
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
