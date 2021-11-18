package cn.leancloud;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCCloudFunctionTest extends TestCase {
  private boolean testSucceed = false;

  public LCCloudFunctionTest(String name) {
    super(name);
    Configure.initializeRuntime();
    LCUser.logOut();
  }

  public static Test suite() {
    return new TestSuite(LCCloudFunctionTest.class);
  }

  public void testCloudFunction() {
    String name = "currentTime";
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<Long> res = LCCloud.callFunctionInBackground(name, param);
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
    Observable<Long> res = LCCloud.callFunctionWithCacheInBackground(name, param,
            LCQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000, Long.class);
    res.subscribe(new Observer<Long>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(Long result) {
        System.out.println("结果 = " + result);
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
    Observable<Long> res = LCCloud.callFunctionWithCacheInBackground(name, param,
            LCQuery.CachePolicy.NETWORK_ONLY, 30000, Long.class);
    res.subscribe(new Observer<Long>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(Long firstResult) {
        System.out.println("第一次结果(NETWORK_ONLY) = " + firstResult);
        final long firstTs = firstResult;
        Observable<Long> second = LCCloud.callFunctionWithCacheInBackground(name, param,
                LCQuery.CachePolicy.NETWORK_ELSE_CACHE, 30000, Long.class);
        second.subscribe(new Observer<Long>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(Long secondResult) {
            System.out.println("第二次结果(NETWORK_ELSE_CACHE) = " + secondResult);
            final long secondTs = (Long) secondResult;
            if (secondTs <= firstTs) {
              System.out.println("the second timestamp is wrong. first-" + firstTs + ", second-" + secondTs);
              latch.countDown();
            } else {
              Observable<Long> third = LCCloud.callFunctionWithCacheInBackground(name, param,
                      LCQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000, Long.class);
              third.subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable disposable) {

                }

                @Override
                public void onNext(Long thirdResult) {
                  System.out.println("第三次结果(CACHE_ELSE_NETWORK) = " + thirdResult);
                  final long thirdTs = (Long) thirdResult;
                  if (secondTs != thirdTs) {
                    System.out.println("the third timestamp is wrong. expected-" + secondTs + ", real-" + thirdTs);
                    latch.countDown();
                  } else {
                    try {
                      Thread.sleep(30000);
                    } catch (Exception ex) {
                      ;
                    }
                    Observable<Long> fourth = LCCloud.callFunctionWithCacheInBackground(name, param,
                            LCQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000, Long.class);
                    fourth.subscribe(new Observer<Long>() {
                      @Override
                      public void onSubscribe(Disposable disposable) {

                      }

                      @Override
                      public void onNext(Long fourthResult) {
                        System.out.println("第四次结果(CACHE_ELSE_NETWORK) = " + fourthResult);
                        final long fourthTs = (Long) fourthResult;
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
