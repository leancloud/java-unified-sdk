package cn.leancloud.core;

import cn.leancloud.Configure;
import cn.leancloud.types.LCDate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class LeanCloudTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LeanCloudTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testServerDate() throws Exception {
    LeanCloud.getServerDateInBackground().subscribe(new Observer<LCDate>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCDate LCDate) {
        testSucceed = true;
        System.out.println(LCDate.toJSONString());
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
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
