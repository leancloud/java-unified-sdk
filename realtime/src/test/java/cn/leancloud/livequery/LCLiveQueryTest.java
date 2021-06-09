package cn.leancloud.livequery;

import cn.leancloud.LCException;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.Configure;
import cn.leancloud.session.LCConnectionManager;
import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCLiveQueryTest extends TestCase{
  private boolean operationSucceed = false;

  public LCLiveQueryTest(String name) {
    super(name);
//    AVIMOptions.getGlobalOptions().setRtmServer("ws://localhost:3000");
//    AVOSCloud.setServer(AVOSService.API, "http://localhost:3000");
    Configure.initialize();
  }


  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
    LCConnectionManager manager = LCConnectionManager.getInstance();
    manager.autoConnection();
    Thread.sleep(6000);
  }

  public void testSubscribe() throws Exception {
    LCQuery query = new LCQuery("Product");
    query.whereExists("objectId");
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("-------- failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          System.out.println("-------- succeed to subscibe livequery");
          liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
            @Override
            public void done(LCException ex) {
              if (null != ex) {
                System.out.println("-------- failed to unsubscribe livequery");
                ex.printStackTrace();
              } else {
                System.out.println("-------- succeed to unsubscribe livequery");
                operationSucceed = true;
              }
              latch.countDown();
            }
          });
        }
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testSubscribeMoreQuery() throws Exception {
    final LCQuery query = new LCQuery("Product");
    query.whereExists("objectId");
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("-------- failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          System.out.println("-------- succeed to subscibe livequery1");
          query.whereLessThan("rating", 3.0);
          final LCLiveQuery liveQuery2 = LCLiveQuery.initWithQuery(query);
          liveQuery2.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
            @Override
            public void done(LCException e2) {
              if (null != e2) {
                System.out.println("-------- failed to subscibe livequery2");
                e2.printStackTrace();
                latch.countDown();
              } else {
                System.out.println("-------- succeed to subscibe livequery2");
                liveQuery2.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
                  @Override
                  public void done(LCException e3) {
                    if (null != e3) {
                      System.out.println("-------- failed to unsubscribe livequery2");
                      e3.printStackTrace();
                      latch.countDown();
                    } else {
                      System.out.println("-------- succeed to unsubscribe livequery2");
                      liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
                        @Override
                        public void done(LCException ex) {
                          if (null != ex) {
                            System.out.println("-------- failed to unsubscribe livequery1");
                            ex.printStackTrace();
                          } else {
                            System.out.println("-------- succeed to unsubscribe livequery1");
                            operationSucceed = true;
                          }
                          latch.countDown();
                        }
                      });
                    }
                  }
                });
              }
            }
          });


        }
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testReceiveLiveQueryCreatedNotification() throws Exception {
    LCQuery query = new LCQuery("Product");
    query.whereExists("objectId");
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
      @Override
      public void onObjectCreated(LCObject LCObject) {
        super.onObjectCreated(LCObject);
        System.out.println("onObjectCreated: " + LCObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          LCObject tmpObj = new LCObject("Product");
          tmpObj.put("title", "IntreStellar");
          tmpObj.put("description", "this is a fiction");
          tmpObj.put("price", 99.90);
          tmpObj.save();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException ex) {
        if (null != ex) {
          System.out.println("failed to unsubscribe livequery");
          ex.printStackTrace();
        } else {
          operationSucceed = true;
        }
        latch2.countDown();
      }
    });
    latch2.await();
    assertTrue(operationSucceed);
  }

  public void testReceiveLiveQueryUpdatedNotification() throws Exception {
    final LCObject baseObject = new LCObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    LCQuery query = new LCQuery("Product");
    query.whereGreaterThan("price", 10.0);
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
      @Override
      public void onObjectUpdated(LCObject LCObject, List<String> updateKeyList) {
        super.onObjectUpdated(LCObject, updateKeyList);
        System.out.println("onObjectUpdated: " + LCObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          baseObject.decrement("price", 40);
          baseObject.save();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException ex) {
        if (null != ex) {
          System.out.println("failed to unsubscribe livequery");
          ex.printStackTrace();
        } else {
          operationSucceed = true;
        }
        baseObject.delete();
        latch2.countDown();
      }
    });
    latch2.await();
    assertTrue(operationSucceed);
  }

  public void testReceiveLiveQueryEnterNotification() throws Exception {
    final LCObject baseObject = new LCObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    LCQuery query = new LCQuery("Product");
    query.whereGreaterThan("price", 100.0);
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
      @Override
      public void onObjectEnter(LCObject LCObject, List<String> updateKeyList) {
        super.onObjectEnter(LCObject, updateKeyList);
        System.out.println("onObjectEnter: " + LCObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          baseObject.increment("price", 100);
          baseObject.save();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException ex) {
        if (null != ex) {
          System.out.println("failed to unsubscribe livequery");
          ex.printStackTrace();
        } else {
          operationSucceed = true;
        }
        baseObject.delete();
        latch2.countDown();
      }
    });
    latch2.await();
    assertTrue(operationSucceed);
  }

  public void testReceiveLiveQueryLeaveNotification() throws Exception {
    final LCObject baseObject = new LCObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    LCQuery query = new LCQuery("Product");
    query.whereLessThan("price", 100.0);
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
      @Override
      public void onObjectLeave(LCObject LCObject, List<String> updateKeyList) {
        super.onObjectLeave(LCObject, updateKeyList);
        System.out.println("onObjectLeave: " + LCObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          baseObject.increment("price", 100);
          baseObject.save();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException ex) {
        if (null != ex) {
          System.out.println("failed to unsubscribe livequery");
          ex.printStackTrace();
        } else {
          operationSucceed = true;
        }
        baseObject.delete();
        latch2.countDown();
      }
    });
    latch2.await();
    assertTrue(operationSucceed);
  }

  public void testReceiveLiveQueryDeleteNotification() throws Exception {
    final LCObject baseObject = new LCObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    LCQuery query = new LCQuery("Product");
    query.whereExists("objectId");
    final LCLiveQuery liveQuery = LCLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new LCLiveQueryEventHandler() {
      @Override
      public void onObjectDeleted(String objectId) {
        super.onObjectDeleted(objectId);
        System.out.println("onObjectDeleted: " + objectId);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          baseObject.delete();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new LCLiveQuerySubscribeCallback() {
      @Override
      public void done(LCException ex) {
        if (null != ex) {
          System.out.println("failed to unsubscribe livequery");
          ex.printStackTrace();
        } else {
          operationSucceed = true;
        }
        latch2.countDown();
      }
    });
    latch2.await();
    assertTrue(operationSucceed);
  }
}
