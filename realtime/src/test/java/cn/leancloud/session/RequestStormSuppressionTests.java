package cn.leancloud.session;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class RequestStormSuppressionTests extends TestCase {
  public RequestStormSuppressionTests(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    RequestStormSuppression.getInstance().cleanup();
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSingleOperation() throws Exception {
    String sessionId = "testUserA";
    String conversationId = "conv-111";
    String identifier = "{'data': {'conversation': 'conv-111fheifhie'}}";
    boolean result = RequestStormSuppression.getInstance().postpone(null);
    assertTrue(!result);
    AVIMOperationQueue.Operation op1 = new AVIMOperationQueue.Operation();
    op1.setIdentifier(identifier);
    op1.sessionId = sessionId;
    op1.conversationId = conversationId;
    op1.operation = 2;
    result = RequestStormSuppression.getInstance().postpone(op1);
    assertTrue(!result);
    result = RequestStormSuppression.getInstance().postpone(op1);
    assertTrue(result);
    AVIMOperationQueue.Operation op2 = new AVIMOperationQueue.Operation();
    op2.setIdentifier(identifier);
    op2.sessionId = sessionId;
    op2.conversationId = conversationId;
    op2.operation = 2;
    result = RequestStormSuppression.getInstance().postpone(op2);
    assertTrue(result);
    assertEquals(1, RequestStormSuppression.getInstance().getCacheSize());
    final CountDownLatch countDownLatch = new CountDownLatch(3);
    RequestStormSuppression.getInstance().release(op1, new RequestStormSuppression.RequestCallback() {
      @Override
      public void done(AVIMOperationQueue.Operation operation) {
        System.out.println("Callback is running....");
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
  }

  public void testMultiOperations() throws Exception {
    String sessionId = "testUserA";
    String conversationId = "conv-111";
    String identifier = "{'data': {'conversation': 'conv-111fheifhie'}}";
    boolean result = RequestStormSuppression.getInstance().postpone(null);
    assertTrue(!result);
    AVIMOperationQueue.Operation op1 = new AVIMOperationQueue.Operation();
    op1.setIdentifier(identifier);
    op1.sessionId = sessionId;
    op1.conversationId = conversationId;
    op1.operation = 2;
    result = RequestStormSuppression.getInstance().postpone(op1);
    assertTrue(!result);
    result = RequestStormSuppression.getInstance().postpone(op1);
    assertTrue(result);
    AVIMOperationQueue.Operation op2 = new AVIMOperationQueue.Operation();
    op2.setIdentifier(identifier);
    op2.sessionId = sessionId;
    op2.conversationId = conversationId;
    op2.operation = 3;
    result = RequestStormSuppression.getInstance().postpone(op2);
    assertTrue(!result);
    assertEquals(2, RequestStormSuppression.getInstance().getCacheSize());
    final CountDownLatch countDownLatch = new CountDownLatch(3);
    RequestStormSuppression.getInstance().release(op1, new RequestStormSuppression.RequestCallback() {
      @Override
      public void done(AVIMOperationQueue.Operation operation) {
        System.out.println("Callback for op1 is running....");
        countDownLatch.countDown();
      }
    });
    RequestStormSuppression.getInstance().release(op2, new RequestStormSuppression.RequestCallback() {
      @Override
      public void done(AVIMOperationQueue.Operation operation) {
        System.out.println("Callback for op2 is running....");
        countDownLatch.countDown();
      }
    });
    RequestStormSuppression.getInstance().release(op2, new RequestStormSuppression.RequestCallback() {
      @Override
      public void done(AVIMOperationQueue.Operation operation) {
        System.out.println("Callback for op2 is running(wrong)....");
        countDownLatch.countDown();
      }
    });
    countDownLatch.await();
  }

  public void testMultiThreads() throws Exception {
    final String sessionId = "testUserA";
    final String conversationId = "conv-111-thread-";
    final String identifier = "{'data': {'conversation': 'conv-111fheifhie'}}";
    final int [] operationCodes = new int[]{11, 23};
    final CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10;i++) {
      final int current = i;
      new Thread(new Runnable() {
        @Override
        public void run() {
          AVIMOperationQueue.Operation op1 = new AVIMOperationQueue.Operation();
          op1.setIdentifier(identifier);
          op1.sessionId = sessionId;
          op1.conversationId = conversationId + Thread.currentThread().getId();
          op1.operation = operationCodes[current % 2];
          boolean needCache = RequestStormSuppression.getInstance().postpone(op1);
          if (needCache) {
            System.out.println("thread " + current + " postponed request after others...");
          } else {
            System.out.println("thread " + current + " postponed request firstly...");
          }
          try {
            Thread.sleep(System.currentTimeMillis() % 1327);
          } catch (Exception ex) {
            ;
          }
          latch.countDown();
        }
      }).start();
    }
    latch.await();
    assertEquals(2, RequestStormSuppression.getInstance().getCacheSize());
    for (int i = 0; i < 10;i++) {
      final int current = i;
      new Thread(new Runnable() {
        @Override
        public void run() {
          AVIMOperationQueue.Operation op1 = new AVIMOperationQueue.Operation();
          op1.setIdentifier(identifier);
          op1.sessionId = sessionId;
          op1.conversationId = conversationId;
          op1.operation = operationCodes[current % 2];
          RequestStormSuppression.getInstance().release(op1, new RequestStormSuppression.RequestCallback() {
            @Override
            public void done(AVIMOperationQueue.Operation operation) {
              System.out.println("thread " + current + " release operation(" + operation.conversationId + ")...");
            }
          });
        }
      }).start();
    }
    try {
      Thread.sleep(2000);
    } catch (Exception ex) {
      ;
    }
    assertEquals(0, RequestStormSuppression.getInstance().getCacheSize());
  }
}
