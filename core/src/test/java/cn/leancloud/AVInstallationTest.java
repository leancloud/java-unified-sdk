package cn.leancloud;

import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.json.JSON;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class AVInstallationTest extends TestCase {
  private boolean testSucceed = false;
  public AVInstallationTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    Thread.sleep(500);
  }

  public void testCreateInstallation() {
    LCInstallation currentInstall = LCInstallation.getCurrentInstallation();
    assertTrue(currentInstall != null);

    LCInstallation install = new LCInstallation();
    assertTrue(install.getInstallationId().length() > 0);
    assertTrue(install.getInstallationId().equals(currentInstall.getInstallationId()));
  }

  public void testAddUnique() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    final LCInstallation install = new LCInstallation();
    install.addUnique("channels", "User-001");
    install.addUnique("channels", "User-001");
    install.addUnique("channels", "User-002");
    install.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = install.getList("channels").size() == 2;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testUnsubscribe() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    final LCInstallation install = new LCInstallation();
    install.addUnique("channels", "User-001");
    install.addUnique("channels", "User-001");
    install.addUnique("channels", "User-002");
    install.addUnique("channels", "User-001");
    install.addUnique("channels", "User-002");
    install.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        install.removeAll("channels", Arrays.asList("User-001"));
        install.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject LCObject) {
            testSucceed = install.getList("channels").size() == 1
                    && install.getList("channels").get(0).equals("User-002");
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            latch.countDown();
          }

          @Override
          public void onComplete() {
          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateInstallationFromOldVersionCache() {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    LCInstallation installation = (LCInstallation) LCObject.parseAVObject(json);
    assertTrue(installation.getInstallationId().equals("fd6605e9a1679d355457ad5c37fc99b3"));
    assertTrue(installation.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    String newJsonString = JSON.toJSONString(installation);
    LCInstallation tmp = (LCInstallation) LCObject.parseAVObject(newJsonString);
    assertTrue(tmp.getInstallationId().equals("fd6605e9a1679d355457ad5c37fc99b3"));
    assertTrue(tmp.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));
  }

  public void testDeserializedFromOldVersionCache() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T06:37:33.258Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"007394934f6a1336718c90e196ef8a64\"}}";
    File installationFile = new File(AppConfiguration.getImportantFileDir(), LeanCloud.getSimplifiedAppId() + LCInstallation.INSTALLATION);
    PersistenceUtil.sharedInstance().saveContentToFile(json, installationFile);
    LCInstallation installation = (LCInstallation) LCObject.parseAVObject(json);
    System.out.println(installation.toString());
    assertTrue(null != installation);
    assertTrue(installation.getInstallationId().equals("007394934f6a1336718c90e196ef8a64"));
    assertTrue(installation.getObjectId().equals("wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG"));

    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;

    installation.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = LCObject.getObjectId().equals("wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG");
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        String errMessage = throwable.getMessage();
        testSucceed = errMessage.indexOf("A unique field was given a value that is already taken") >= 0;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
  public void testSaveInstallation() throws Exception {
    LCInstallation currentInstall = LCInstallation.getCurrentInstallation();
    currentInstall.saveInBackground().blockingFirst();
    Thread.sleep(100);
  }

  public void testSaveInstallationWithCustomProp() {
    LCInstallation currentInstall = LCInstallation.getCurrentInstallation();
    currentInstall.put("chan", "Chan");
    currentInstall.addAll("course", Arrays.asList("Artist"));
    currentInstall.saveInBackground().blockingFirst();
    currentInstall.remove("chan");
    currentInstall.removeAll("course", Arrays.asList("Artist", "Reading"));
    currentInstall.removeAll("course", Arrays.asList("Sport"));
    currentInstall.saveInBackground().blockingFirst();
  }

  public void testSaveWithPointerAndDate() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    LCInstallation currentInstall = LCInstallation.getCurrentInstallation();
    currentInstall.put("date", new Date());

    LCUser user = LCObject.createWithoutData(LCUser.class, "5dd73208844bb40074b18fd7");
    currentInstall.put("user", user);

    currentInstall.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testUpdateWithPointerAndArray() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    LCInstallation currentInstall = LCInstallation.getCurrentInstallation();
    currentInstall.fetch("user");
    LCUser user = currentInstall.getLCObject("user");
    if (null != user) {
      assertEquals(user.getUsername(), "jfeng");
    } else {
      user = LCObject.createWithoutData(LCUser.class, "5dd73208844bb40074b18fd7");
      currentInstall.put("user", user);
    }
    currentInstall.put("channel", Arrays.asList("Public", "People", "Open"));
    currentInstall.saveInBackground()
            .subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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
