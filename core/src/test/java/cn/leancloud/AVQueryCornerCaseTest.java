package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVQueryCornerCaseTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  public AVQueryCornerCaseTest(String testName) {
    super(testName);
    Configure.initializeWithApp("some-app-id", "some-app-key",
            "https://yours.domain.com");
  }

  public static Test suite() {
    return new TestSuite(AVQueryCornerCaseTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
  }

  public void testArrayQuery() throws Exception {
    AVQuery<AVObject> query = new AVQuery<>("homeClass");
    query.whereEqualTo("tags", "女孩");
    query.whereEqualTo("picpass", 1);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull List<AVObject> avObjects) {
        System.out.println("get results. count=" + avObjects.size());
        testSucceed = null != avObjects && avObjects.size() > 10;
        latch.countDown();
      }

      @Override
      public void onError(@NotNull Throwable throwable) {
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
