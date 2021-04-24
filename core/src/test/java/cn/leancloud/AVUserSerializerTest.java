package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;

public class AVUserSerializerTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public static final String USERNAME = "jfeng20200618";
  public static final String PASSWORD = "FER$@$@#Ffwe";
  private static final String EMAIL = "jfeng@test.com";

  public AVUserSerializerTest(String name) {
    super(name);
    Configure.initializeRuntime();
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
    AVUser.loginByEmail(EMAIL, PASSWORD).subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull AVUser avUser) {
        AVFile avatar = new AVFile("test avatar", "https://nimg.ws.126.net/?url=http%3A%2F%2Fcms-bucket.ws.126.net%2F2021%2F0424%2Fe1bb4ab1j00qs1sfw000mc000go00b4c.jpg&thumbnail=660x2147483647&quality=80&type=jpg");
        avUser.put("avatar", avatar);
        avUser.save();
        AVFile savedFile = avUser.getAVFile("avatar");
        testSucceed = null != savedFile;
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
    AVUser user = AVUser.currentUser();
    System.out.println(user);
    assertTrue(null != user);
    AVFile savedFile = user.getAVFile("avatar");
    System.out.println(savedFile);
    assertTrue(null != savedFile);
  }

  public void testDeserializedUser() throws Exception {
    String jsonString = "{ \"_version\":\"5\",\"className\":\"_User\"," +
            "\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"signDate\":new Date(1594310400000)," +
            "\"username\":\"变音小助手\",\"siOffDate\":new Date(1594310400000)}}";
    AVUser user = (AVUser) AVObject.parseAVObject(jsonString);
    assertTrue(null != user);

    try {
      // gson doesnot support "new Date".
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
