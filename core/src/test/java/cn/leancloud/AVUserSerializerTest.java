package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class AVUserSerializerTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVUserSerializerTest(String name) {
    super(name);
    Configure.initializeWithMasterKey("xtuccgojwm9z701f4wzyu579klvlmag2pugywe39rg5iyqug",
            "uzhzd5etmy4i6r3hbnzxhzbfojhfy8pw87fx8ve04w2h6id0", "https://lc.i7play.com");
  }

  public static Test suite() {
    return new TestSuite(AVUserSerializerTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testUserFetch() throws Exception {
    final AVUser user = AVObject.createWithoutData(AVUser.class, "5c83c5b9303f390065666111");
    user.fetchInBackground("author,kuolie,black").subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject object) {
        System.out.println(user);
        AVUser.changeCurrentUser(user, true);
        testSucceed = true;
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

  public void testCurrentUserFromLocalCache() throws Exception {
    AVUser user = AVUser.currentUser();
    assertTrue(null != user);
    System.out.println(user);
  }

  public void testDeserializedUser() throws Exception {
    String jsonString = "{ \"_version\":\"5\",\"className\":\"_User\"," +
            "\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"signDate\":new Date(1594310400000)," +
            "\"username\":\"变音小助手\",\"siOffDate\":new Date(1594310400000)}}";
    AVUser user = (AVUser) AVObject.parseAVObject(jsonString);
    assertTrue(null != user);

    try {
      jsonString = "{ \"_version\":\"5\",\"className\":\"_User\"," +
              "\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"signDate\":new Date(1594310400000)," +
              "\"sessionToken\":[new Date(1594310200000), new Date(1594310420000)],\"username\":\"变音小助手\",\"siOffDate\":new Date(1594310400000)}}";
      user = (AVUser) AVObject.parseAVObject(jsonString);
      assertTrue(null != user);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }
}
