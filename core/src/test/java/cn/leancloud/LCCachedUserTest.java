package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

public class LCCachedUserTest extends TestCase {
  private boolean operationSucceed = false;
  public static final String USERNAME = "jfeng20200618";
  public static final String PASSWORD = "FER$@$@#Ffwe";
  private static final String EMAIL = "jfeng20200618@test.com";
  public LCCachedUserTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }
  public void testQueryUser() throws Exception {
    String userSessionToken = "q5891pin0s7oud6x651k9dtvz";
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.becomeWithSessionToken(userSessionToken).refreshInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull LCObject lcObject) {
        System.out.println("refresh result: "+ lcObject.toJSONString());
        LCFile iconFile = lcObject.getLCFile("icon");
        System.out.println("icon result: "+ iconFile.toJSONString());
        LCUser.changeCurrentUser((LCUser) lcObject, true);
        operationSucceed = true;
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
    assertEquals(true, operationSucceed);
  }

  public void testGetUserFromCache() throws Exception {
    LCUser currentUser = LCUser.currentUser();
    System.out.println("refresh result: "+ currentUser.toJSONString());
    LCFile iconFile = currentUser.getLCFile("icon");
    operationSucceed = null != iconFile;
    System.out.println("icon result: "+ iconFile.toJSONString());
    assertEquals(true, operationSucceed);
  }

}
