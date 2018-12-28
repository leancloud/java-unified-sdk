package cn.leancloud;

import cn.leancloud.cache.PersistenceUtil;
import cn.leancloud.core.AppConfiguration;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AVInstallationTest extends TestCase {
  private boolean testSucceed = false;
  public AVInstallationTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }

  public void testCreateInstallation() {
    AVInstallation install = new AVInstallation();
    assertTrue(install.getInstallationId().length() > 0);
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    assertTrue(currentInstall != null);
    assertTrue(install.getInstallationId().equals(currentInstall.getInstallationId()));
  }

  public void testCreateInstallationFromOldVersionCache() {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    AVInstallation installation = (AVInstallation) AVObject.parseAVObject(json);
    assertTrue(installation.getInstallationId().equals("fd6605e9a1679d355457ad5c37fc99b3"));
    assertTrue(installation.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    String newJsonString = JSON.toJSONString(installation, ObjectValueFilter.instance,
            SerializerFeature.WriteClassName,
            SerializerFeature.DisableCircularReferenceDetect);
    AVInstallation tmp = (AVInstallation) AVObject.parseAVObject(newJsonString);
    assertTrue(tmp.getInstallationId().equals("fd6605e9a1679d355457ad5c37fc99b3"));
    assertTrue(tmp.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));
  }

  public void testDeserializedFromOldVersionCache() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T06:37:33.258Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"007394934f6a1336718c90e196ef8a64\"}}";
    File installationFile = new File(AppConfiguration.getImportantFileDir(), AVInstallation.INSTALLATION);
    PersistenceUtil.sharedInstance().saveContentToFile(json, installationFile);
    AVInstallation installation = AVInstallation.getCurrentInstallation();
    assertTrue(null != installation && installation.getInstallationId().equals("007394934f6a1336718c90e196ef8a64"));
    assertTrue(installation.getObjectId().equals("wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG"));

    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;

    installation.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        testSucceed = avObject.getObjectId().equals("wYtTtsc5jnd0tXX8hQQa8oBekQXHBUIG");
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
  public void testSaveInstallation() {
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    currentInstall.saveInBackground().blockingFirst();
  }
  public void testSaveInstallationWithCustomProp() {
    AVInstallation currentInstall = AVInstallation.getCurrentInstallation();
    currentInstall.put("chan", "Chan");
    currentInstall.saveInBackground().blockingFirst();
  }
}
