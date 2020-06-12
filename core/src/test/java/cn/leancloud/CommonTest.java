package cn.leancloud;

import cn.leancloud.json.JSON;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommonTest extends TestCase {
  public CommonTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CommonTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.out.println("exit setUp()");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    System.out.println("exit tearDown()");
  }

  public void testPutAllForMap() throws Exception {
    Map<String, Object> hashMap = new HashMap<>();
    try {
      hashMap.put("testKey", null);
      System.out.println("We could put null value to hash Map.");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ConcurrentMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>();
    try {
      concurrentHashMap.put("testKey", null);
    } catch (Exception ex) {
      System.out.println("We could not put null value to concurrent Map.");
      ex.printStackTrace();
    }

    Map<String, Object> synchronizedMap = Collections.synchronizedMap(new HashMap<String, Object>());
    synchronizedMap.put("testKey", null);
  }

  public void testJsonDeserialization() throws Exception {
    double a = 2.65D;
    System.out.println(a);
    String dataString = "{\"audio\":{\"type\":\"cn.leancloud.AVFile\",\"bucket\":\"xtuccgoj\",\"dataAvailable\":true," +
            "\"dirty\":false,\"metaData\":{\"@type\":\"java.util.HashMap\",\"owner\":\"5c83c5b9303f390065666111\",\"_checksum\":\"92b0d717e56b77e28976c85433830ad8\",\"size\":8986,\"_name\":\"kuo_audio_1575980353771.mp3\"}," +
            "\"name\":\"5def8d41fc36ed0068874955\",\"objectId\":\"5def8d41fc36ed0068874955\",\"originalName\":\"kuo_audio_1575980353771.mp3\"," +
            "\"ownerObjectId\":\"5c83c5b9303f390065666111\",\"size\":8986,\"url\":\"http://file2.i7play.com/vCiVRj7RBmHSSwyQji815TM8KxY4Umx2NMA6Cg6W.mp3\"},\"value\":0.5D}";
    JSON.parse(dataString);
  }

  public void testConvertRecurCallback() throws Exception {
    final String mobilePhone = "";
    final String smsCode = "";
    AVUser.signUpOrLoginByMobilePhoneInBackground(mobilePhone, smsCode).flatMap(new Function<AVUser, Observable<? extends AVObject>>() {
      @Override
      public Observable<? extends AVObject> apply(AVUser avUser) throws Exception {
        String username = "";
        String nickname = "";
        avUser.setUsername(username);
        avUser.put("nickname", nickname);
        return avUser.saveInBackground();
      }
    }).flatMap(new Function<AVObject, Observable<? extends AVObject>>() {
      @Override
      public Observable<? extends AVObject> apply(AVObject avUser) throws Exception {
        AVInstallation currentInstallation = AVInstallation.getCurrentInstallation();
        currentInstallation.put("user", avUser.getObjectId());
        return currentInstallation.saveInBackground();
      }
    }).subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {

      }

      @Override
      public void onError(Throwable throwable) {

      }

      @Override
      public void onComplete() {

      }
    });
  }
}
