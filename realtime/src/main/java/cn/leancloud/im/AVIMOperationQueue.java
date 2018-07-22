package cn.leancloud.im;

import cn.leancloud.command.CommandPacket;
import cn.leancloud.session.MessageQueue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AVIMOperationQueue {
  public static class Operation {
    int requestId;
    int operation;
    String sessionId;
    String conversationId;

    public static Operation getOperation(int operation, String sessionId, String conversationId,
                                         int requestId) {
      Operation op = new Operation();
      op.conversationId = conversationId;
      op.sessionId = sessionId;
      op.operation = operation;
      op.requestId = requestId;
      return op;
    }
  }

  static ConcurrentHashMap<Integer, Runnable> timeoutCache =
          new ConcurrentHashMap<Integer, Runnable>();
  Map<Integer, Operation> cache = new ConcurrentHashMap<>();
  MessageQueue<Operation> operationQueue;

  public AVIMOperationQueue(String key) {
    operationQueue =
            new MessageQueue<AVIMOperationQueue.Operation>("operation.queue." + key, Operation.class);
    setupCache();
  }
  private void setupCache() {
    for (Operation op : operationQueue) {
      if (op.requestId != CommandPacket.UNSUPPORTED_OPERATION) {
        cache.put(op.requestId, op);
      }
    }
  }
  public void offer(final Operation op) {
    ;
  }
  public Operation poll(int requestId) {
    return null;
  }
  public Operation poll() {
    return operationQueue.poll();
  }
  public void clear() {
    operationQueue.clear();
    cache.clear();
  }
  public boolean isEmpty() {
    return operationQueue.isEmpty();
  }
}
