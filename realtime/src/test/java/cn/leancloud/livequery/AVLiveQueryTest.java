package cn.leancloud.livequery;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AVOSService;
import cn.leancloud.im.AVIMOptions;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVLiveQueryTest extends TestCase{
  private boolean operationSucceed = false;

  public AVLiveQueryTest(String name) {
    super(name);
//    AVIMOptions.getGlobalOptions().setRtmServer("ws://localhost:3000");
//    AVOSCloud.setServer(AVOSService.API, "http://localhost:3000");
    Configure.initialize();
  }


  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.autoConnection();
    Thread.sleep(6000);
  }

  public void testSubscribe() throws Exception {
    AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          System.out.println("-------- failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          System.out.println("-------- succeed to subscibe livequery");
          liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
            @Override
            public void done(AVException ex) {
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
    final AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          System.out.println("-------- failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          System.out.println("-------- succeed to subscibe livequery1");
          query.whereLessThan("rating", 3.0);
          final AVLiveQuery liveQuery2 = AVLiveQuery.initWithQuery(query);
          liveQuery2.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
            @Override
            public void done(AVException e2) {
              if (null != e2) {
                System.out.println("-------- failed to subscibe livequery2");
                e2.printStackTrace();
                latch.countDown();
              } else {
                System.out.println("-------- succeed to subscibe livequery2");
                liveQuery2.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
                  @Override
                  public void done(AVException e3) {
                    if (null != e3) {
                      System.out.println("-------- failed to unsubscribe livequery2");
                      e3.printStackTrace();
                      latch.countDown();
                    } else {
                      System.out.println("-------- succeed to unsubscribe livequery2");
                      liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
                        @Override
                        public void done(AVException ex) {
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
    AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
      @Override
      public void onObjectCreated(AVObject avObject) {
        super.onObjectCreated(avObject);
        System.out.println("onObjectCreated: " + avObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          AVObject tmpObj = new AVObject("Product");
          tmpObj.put("title", "IntreStellar");
          tmpObj.put("description", "this is a fiction");
          tmpObj.put("price", 99.90);
          tmpObj.save();
        }
      }
    });
    latch.await();

    final CountDownLatch latch2 = new CountDownLatch(1);
    liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException ex) {
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
    final AVObject baseObject = new AVObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    AVQuery query = new AVQuery("Product");
    query.whereGreaterThan("price", 10.0);
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
      @Override
      public void onObjectUpdated(AVObject avObject, List<String> updateKeyList) {
        super.onObjectUpdated(avObject, updateKeyList);
        System.out.println("onObjectUpdated: " + avObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
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
    liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException ex) {
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
    final AVObject baseObject = new AVObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    AVQuery query = new AVQuery("Product");
    query.whereGreaterThan("price", 100.0);
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
      @Override
      public void onObjectEnter(AVObject avObject, List<String> updateKeyList) {
        super.onObjectEnter(avObject, updateKeyList);
        System.out.println("onObjectEnter: " + avObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
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
    liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException ex) {
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
    final AVObject baseObject = new AVObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    AVQuery query = new AVQuery("Product");
    query.whereLessThan("price", 100.0);
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
      @Override
      public void onObjectLeave(AVObject avObject, List<String> updateKeyList) {
        super.onObjectLeave(avObject, updateKeyList);
        System.out.println("onObjectLeave: " + avObject);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
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
    liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException ex) {
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
    final AVObject baseObject = new AVObject("Product");
    baseObject.put("title", "IntreStellar");
    baseObject.put("description", "this is a fiction");
    baseObject.put("price", 99.90);
    baseObject.save();

    AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    final AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    final CountDownLatch latch = new CountDownLatch(1);
    liveQuery.setEventHandler(new AVLiveQueryEventHandler() {
      @Override
      public void onObjectDeleted(String objectId) {
        super.onObjectDeleted(objectId);
        System.out.println("onObjectDeleted: " + objectId);
        latch.countDown();
      }
    });
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
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
    liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException ex) {
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
