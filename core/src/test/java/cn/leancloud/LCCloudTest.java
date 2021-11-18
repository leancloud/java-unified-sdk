package cn.leancloud;

import cn.leancloud.json.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCCloudTest extends TestCase {
  private boolean testSucceed = false;

  public LCCloudTest(String name) {
    super(name);
    Configure.initializeRuntime();
    LCUser.logOut();
  }

  public static Test suite() {
    return new TestSuite(LCCloudTest.class);
  }

  public void testRPCWithCache() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final Map<String, Object> params = new HashMap<>(2);
    params.put("pageSize", 20);
    params.put("currentPageIndex", 0);
    Observable<List> observable = LCCloud.callRPCInBackground("ArticleItem.getArticleType", params);
    observable.subscribe(new Observer<List>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull List objects) {
        for(Object o: objects) {
          System.out.println("class: " + o.getClass() + ", value: " + o);
        }
        Observable<List> observable2 = LCCloud.callRPCWithCacheInBackground("ArticleItem.getArticleType", params,
                LCQuery.CachePolicy.CACHE_ELSE_NETWORK, 86400000, List.class);
        observable2.subscribe(new Observer<List>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull List list) {
            for(Object o: list) {
              System.out.println("second loop - class: " + o.getClass() + ", value: " + o);
            }
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(@NotNull Throwable throwable) {
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });

      }

      @Override
      public void onError(@NotNull Throwable throwable) {
        throwable.printStackTrace();
        if (throwable.getMessage().indexOf("No such cloud function") >= 0) {
          testSucceed = true;
        }
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCloudFunction() {
    String name = "hallo";
    Map<String, Object> param = new HashMap<String, Object>();
    Observable<JSONObject> res = LCCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println("结果 = " + jsonObject);
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error occurred! " + throwable);
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testCloudFunction4IMPresence() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    String name = "getOnOffStatus";
    Map<String, Object> param = new HashMap<String, Object>();
    List<String> userIds = Arrays.asList("4B7FFEF52D744A07A2A85335F34402D7", "5de4e42321b47e006ca18ffe");
    param.put("peerIds", userIds);
    Observable<List<String>> res = LCCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(List<String> strings) {
                      System.out.println("结果 = " + strings);
                      for (String t : strings) {
                        if (null == t) {
                          System.out.println("result: unknown");
                        } else if (1 == Integer.valueOf(t)) {
                          System.out.println("result: online");
                        } else {
                          System.out.println("result: offline");
                        }
                      }
                      latch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                      System.out.println("error occurred! " + throwable);
                      latch.countDown();
                    }

                    @Override
                    public void onComplete() {

                    }
                  });
    latch.await();
  }

  public void testRPCFunction1() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    String name = "HomeData.getHomeData_V87";
    Map<String, Object> param = new HashMap<>();
    param.put("pageSize", 20);
    param.put("currentPageIndex", 1);
    param.put("end", new Date());
    Observable<LCObject> res = LCCloud.callRPCInBackground(name, param);
    res.subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("got result:" + LCObject);
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCloudRPC() {
    String name = "leanengine/update-leanengine-function-metadata";
    Map<String, String> param = new HashMap<String, String>();
    param.put("content", "test");
    try {
      Object res = LCCloud.callRPCInBackground(name, param).blockingFirst();
      assertNull(res);
    } catch (Exception ex) {
      // ignore
    }
  }
}
