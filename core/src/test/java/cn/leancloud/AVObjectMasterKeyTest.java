package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AVObjectMasterKeyTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVObjectMasterKeyTest(String name) {
    super(name);
    Configure.initializeRuntime();

  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    AVOSCloud.setMasterKey("your master key");
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
    AVOSCloud.setMasterKey(null);
  }

  public void testCreateObject() throws Exception {
    AVObject object = new AVObject("StrictObject");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("saveObject finished.");
        latch.countDown();

      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("saveObject failed.");
        throwable.printStackTrace();
        latch.countDown();
        testSucceed = true;
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
