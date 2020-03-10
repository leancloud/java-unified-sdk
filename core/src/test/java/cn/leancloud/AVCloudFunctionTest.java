package cn.leancloud;

import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AVCloudFunctionTest extends TestCase {
  private boolean testSucceed = false;

  public AVCloudFunctionTest(String name) {
    super(name);
    Configure.initializeRuntime();
    AVUser.logOut();
  }

  public static Test suite() {
    return new TestSuite(AVCloudFunctionTest.class);
  }

  public void testCloudFunction() {
    String name = "currentTime";
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<Long> res = AVCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<Long>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(Long jsonObject) {
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
  public void testCloudFunctionWithCache() {
    String name = "currentTime";
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<HashMap<String, Object>> res = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000);
    res.subscribe(new Observer<HashMap<String, Object>>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(HashMap<String, Object> jsonObject) {
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

  public void testCloudFunctionWithCacheMoreTimes() throws Exception {
    testSucceed = false;
    final CountDownLatch latch = new CountDownLatch(1);
    final String name = "currentTime";
    final Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<HashMap<String, Object>> res = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.NETWORK_ONLY, 30000);
    res.subscribe(new Observer<HashMap<String, Object>>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(HashMap<String, Object> firstResult) {
        System.out.println("第一次结果(NETWORK_ONLY) = " + firstResult);
        final long firstTs = (Long) firstResult.get("milliseconds");
        Observable<HashMap<String, Object>> second = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.NETWORK_ELSE_CACHE, 30000);
        second.subscribe(new Observer<HashMap<String, Object>>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(HashMap<String, Object> secondResult) {
            System.out.println("第二次结果(NETWORK_ELSE_CACHE) = " + secondResult);
            final long secondTs = (Long) secondResult.get("milliseconds");
            if (secondTs <= firstTs) {
              System.out.println("the second timestamp is wrong. first-" + firstTs + ", second-" + secondTs);
              latch.countDown();
            } else {
              Observable<HashMap<String, Object>> third = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000);
              third.subscribe(new Observer<HashMap<String, Object>>() {
                @Override
                public void onSubscribe(Disposable disposable) {

                }

                @Override
                public void onNext(HashMap<String, Object> thirdResult) {
                  System.out.println("第三次结果(CACHE_ELSE_NETWORK) = " + thirdResult);
                  final long thirdTs = (Long) thirdResult.get("milliseconds");
                  if (secondTs != thirdTs) {
                    System.out.println("the third timestamp is wrong. expected-" + secondTs + ", real-" + thirdTs);
                    latch.countDown();
                  } else {
                    try {
                      Thread.sleep(30000);
                    } catch (Exception ex) {
                      ;
                    }
                    Observable<HashMap<String, Object>> fourth = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000);
                    fourth.subscribe(new Observer<HashMap<String, Object>>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(HashMap<String, Object> fourthResult) {
                        System.out.println("第四次结果(CACHE_ELSE_NETWORK) = " + fourthResult);
                        final long fourthTs = (Long) fourthResult.get("milliseconds");
                        if (fourthTs <= thirdTs) {
                          System.out.println("the fourth timestamp is wrong. expected-" + secondTs + ", real-" + thirdTs);
                        } else {
                          testSucceed = true;
                        }
                        latch.countDown();
                      }

                      @Override
                      public void onError(Throwable throwable) {
                        System.out.println("fourth error occurred! " + throwable);
                        latch.countDown();
                      }

                      @Override
                      public void onComplete() {

                      }
                    });
                  }
                }

                @Override
                public void onError(Throwable throwable) {
                  System.out.println("third error occurred! " + throwable);
                  latch.countDown();
                }

                @Override
                public void onComplete() {

                }
              });
            }
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("second error occurred! " + throwable);
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("first error occurred! " + throwable);
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
