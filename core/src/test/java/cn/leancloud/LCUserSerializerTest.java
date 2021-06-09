package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

public class LCUserSerializerTest extends UserBasedTestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  private String testUserObjectId = null;

  public LCUserSerializerTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(LCUserSerializerTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testSucceed = false;
    latch = new CountDownLatch(1);
    if (null != LCUser.getCurrentUser()) {
      testUserObjectId = LCUser.getCurrentUser().getObjectId();
    } else {
      testUserObjectId = null;
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testUserFetch() throws Exception {
    final LCUser user = LCObject.createWithoutData(LCUser.class, testUserObjectId);
    user.fetchInBackground("author,kuolie,black").subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(LCObject object) {
        System.out.println(user);
        LCUser.changeCurrentUser(user, true);
        testSucceed = true;
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

  public void testCurrentUserFromLocalCache() throws Exception {
  }

  public void testDeserializedUser() throws Exception {
    String jsonString = "{ \"_version\":\"5\",\"className\":\"_User\"," +
            "\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"signDate\":new Date(1594310400000)," +
            "\"username\":\"变音小助手\",\"siOffDate\":new Date(1594310400000)}}";
    LCUser user = (LCUser) LCObject.parseLCObject(jsonString);
    assertTrue(null != user);

    try {
      // gson doesnot support "new Date".
      jsonString = "{ \"_version\":\"5\",\"className\":\"_User\"," +
              "\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"signDate\":new Date(1594310400000)," +
              "\"sessionToken\":[new Date(1594310200000), new Date(1594310420000)],\"username\":\"变音小助手\",\"siOffDate\":new Date(1594310400000)}}";
      user = (LCUser) LCObject.parseLCObject(jsonString);
      assertTrue(null != user);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}
