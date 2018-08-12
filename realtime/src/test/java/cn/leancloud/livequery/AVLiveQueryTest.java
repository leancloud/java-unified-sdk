package cn.leancloud.livequery;

import cn.leancloud.AVException;
import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.Configure;
import cn.leancloud.session.AVConnectionManager;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVLiveQueryTest extends TestCase{
  private boolean operationSucceed = false;

  public AVLiveQueryTest(String name) {
    super(name);
    Configure.initialize();
  }


  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
    AVConnectionManager manager = AVConnectionManager.getInstance();
    manager.startConnection();
    Thread.sleep(6000);
  }

  public void testSubscribe() throws Exception {
    AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    CountDownLatch latch = new CountDownLatch(1);
    liveQuery.subscribeInBackground(new AVLiveQuerySubscribeCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          System.out.println("failed to subscibe livequery");
          e.printStackTrace();
          latch.countDown();
        } else {
          liveQuery.unsubscribeInBackground(new AVLiveQuerySubscribeCallback() {
            @Override
            public void done(AVException ex) {
              if (null != ex) {
                System.out.println("failed to unsubscribe livequery");
                ex.printStackTrace();
              } else {
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

  public void testReceiveLiveQueryNotification() throws Exception {
    AVQuery query = new AVQuery("Product");
    query.whereExists("objectId");
    AVLiveQuery liveQuery = AVLiveQuery.initWithQuery(query);
    CountDownLatch latch = new CountDownLatch(1);
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

    CountDownLatch latch2 = new CountDownLatch(1);
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
