package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.LogUtil;
import com.alibaba.fastjson.JSON;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AVObjectSerializer2Test extends TestCase {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVObjectSerializer2Test.class);
  private static final String CLASSNAME_STUDENT = "Student";
  private static final String FILE_OBJECT_ID = "5bff45249f54540066d4d829";
  private String studentId = null;
  public AVObjectSerializer2Test(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectSerializer2Test.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testComplexObjectDescerializer() {
    AVQuery q = new AVQuery("Student");
    q.orderByDescending("createdAt");
    q.whereExists("portrait");
    AVObject o = q.getFirst();
//    if (o.getAVFile("portrait") == null) {
//      AVFile portrait = new AVFile("thumbnail", "https://tvax1.sinaimg.cn/crop.0.0.200.200.180/a8d43f7ely1fnxs86j4maj205k05k74f.jpg");
//      o.put("portrait", portrait);
//      o.save();
//    }
    System.out.println(o.toJSONString());
    AVObject newO = AVObject.parseAVObject(o.toJSONString());
    assertTrue(null != newO);
    assertTrue(null != newO.getAVFile("portrait"));

    String arhiveString = ArchivedRequests.getArchiveContent(o, false);
    System.out.println(arhiveString);
    AVObject newV = ArchivedRequests.parseAVObject(arhiveString);
    System.out.println(newV.toJSONString());
    assertTrue(null != newV);
    assertTrue(null != newV.getAVFile("portrait"));
  }

  public void testDeserializAVObject() throws Exception {
    String json = "{ \"@type\":\"com.avos.avoscloud.AVInstallation\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Installation\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    AVInstallation installation = (AVInstallation) AVObject.parseAVObject(json);
    assertTrue(null != installation);
    assertTrue(installation.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVObject\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"Student\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    AVObject student = (AVObject) AVObject.parseAVObject(json);
    assertTrue(null != student);
    assertTrue(student.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVUser\",\"objectId\":\"5c282efc9f54540070f04e9a\",\"updatedAt\":\"2018-12-30T02:35:40.331Z\",\"createdAt\":\"2018-12-30T02:35:40.331Z\",\"className\":\"_User\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"emailVerified\":false,\"password\":\"qwertyuiop\",\"sessionToken\":\"bckq6t7gvvfns2t7eer9imxq1\",\"portrait\":{\"@type\":\"com.avos.avoscloud.AVFile\",\"dataAvailable\":true,\"dirty\":false,\"metaData\":{\"@type\":\"java.util.HashMap\",\"_name\":\"thumbnail\",\"__source\":\"external\"},\"name\":\"5c282efc808ca4565c079bc6\",\"objectId\":\"5c282efc808ca4565c079bc6\",\"originalName\":\"thumbnail\",\"size\":-1,\"url\":\"https://tvax1.sinaimg.cn/crop.0.0.200.200.180/a8d43f7ely1fnxs86j4maj205k05k74f.jpg\"},\"mobilePhoneVerified\":false,\"username\":\"er@a.com\"}}";
    AVUser user = (AVUser) AVObject.parseAVObject(json);
    assertTrue(null != user);
    assertTrue(user.getObjectId().equals("5c282efc9f54540070f04e9a"));
    AVFile userProfile = user.getAVFile("portrait");
    assertTrue(null != userProfile);

    json = "{ \"@type\":  \"com.avos.avoscloud.AVStatus\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_Status\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    AVStatus status = (AVStatus) AVObject.parseAVObject(json);
    assertTrue(null != status);
    assertTrue(status.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    json = "{ \"@type\":\"com.avos.avoscloud.AVFile\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"_File\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
    AVFile file = (AVFile) AVObject.parseAVObject(json);
    assertTrue(null != file);
    assertTrue(file.getObjectId().equals("0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs"));

    try {
      json = "{ \"@type\":\"com.avos.avoscloud.AVPushOOO\",\"objectId\":\"0qYaOiU08hqm8bgpDk4CrTXXBs1NPtSs\",\"updatedAt\":null,\"createdAt\":\"2018-12-28T03:16:19.239Z\",\"className\":\"Push\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"deviceType\":\"android\",\"timeZone\":\"Asia/Shanghai\",\"installationId\":\"fd6605e9a1679d355457ad5c37fc99b3\"}}";
      AVObject o = (AVObject) AVObject.parseAVObject(json);
      System.out.println(o);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
