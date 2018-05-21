package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;

public class AVObjectEventuallyTest extends TestCase {
  private static final String CLASSNAME_STUDENT = "Student";
  private NetworkingDetectorMock networkingDetectorMock = new NetworkingDetectorMock();

  public AVObjectEventuallyTest(String name) {
    super(name);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
    AppConfiguration.setGlobalNetworkingDetector(this.networkingDetectorMock);
  }

  public static Test suite() {
    return new TestSuite(AVObjectEventuallyTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    this.networkingDetectorMock.setConnected(true);
  }

  public void testSaveDummy() {
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    AVQuery<AVObject> query = new AVQuery(CLASSNAME_STUDENT);
    try {
      int cntBefore = query.count();
      object.saveEventually();
      int cntAfter = query.count();
      System.out.println("cntBefore: " + cntBefore + ", cntAfter: " + cntAfter);
      assertEquals(cntAfter, cntBefore);
    } catch (AVException ex) {
      fail();
    }
  }

  public void testSaveDirectly() {
    AVQuery<AVObject> query = new AVQuery(CLASSNAME_STUDENT);
    int cntBefore = query.count();
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("occ", new Date());
    try {
      object.saveEventually();
      Thread.sleep(2000);
      int cntAfter = query.count();
      System.out.println("cntBefore: " + cntBefore + ", cntAfter: " + cntAfter);
      assertEquals(cntAfter, cntBefore + 1);
    } catch (AVException ex) {
      ex.printStackTrace();
      fail();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      fail();
    }
  }

  public void testSaveEventually() {
    AVQuery<AVObject> query = new AVQuery(CLASSNAME_STUDENT);
    int cntBefore = query.count();
    this.networkingDetectorMock.setConnected(false);
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.add("course", "Art");
    object.put("occ", new Date());
    try {
      object.saveEventually();
      this.networkingDetectorMock.setConnected(true);
      Thread.sleep(20000);
      int cntAfter = query.count();
      System.out.println("cntBefore: " + cntBefore + ", cntAfter: " + cntAfter);
      assertEquals(cntAfter, cntBefore + 1);
    } catch (AVException ex) {
      ex.printStackTrace();
      fail();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      fail();
    }
  }

  public void testSaveArchived() {
    try {
      ArchivedRequests requests = ArchivedRequests.getInstance();
      Thread.sleep(20000);
    } catch (Exception ex) {
      fail();
    }
  }

  public void testDeleteInvalidObject() {
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.deleteEventually();
  }

  public void testDeleteDirectly() {
    AVObject obj = AVObject.createWithoutData(CLASSNAME_STUDENT, "5b029e7fa22b9d0044bbef2a");
    obj.deleteEventually();
  }

  public void testDeleteEventually() {
    AVObject obj = AVObject.createWithoutData(CLASSNAME_STUDENT, "5b0285889f54542e39416143");
    this.networkingDetectorMock.setConnected(false);
    obj.deleteEventually();
    this.networkingDetectorMock.setConnected(true);
    try {
      Thread.sleep(20000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


}
