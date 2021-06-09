package cn.leancloud;

import cn.leancloud.core.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class LCObjectMasterKeyTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LCObjectMasterKeyTest(String name) {
    super(name);
    Configure.initializeRuntime();

  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    LeanCloud.setMasterKey("your master key");
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
    LeanCloud.setMasterKey(null);
  }

  public void testCreateObject() throws Exception {
    LCObject object = new LCObject("StrictObject");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
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
