package cn.leancloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import junit.framework.TestCase;

public class AVInstallationTest extends TestCase {
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
