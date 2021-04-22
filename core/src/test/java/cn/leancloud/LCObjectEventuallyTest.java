package cn.leancloud;

import cn.leancloud.core.AppConfiguration;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;

public class LCObjectEventuallyTest extends TestCase {
  private static final String CLASSNAME_STUDENT = "Student";
  private NetworkingDetectorMock networkingDetectorMock = new NetworkingDetectorMock();

  public LCObjectEventuallyTest(String name) {
    super(name);
    AppConfiguration.setGlobalNetworkingDetector(this.networkingDetectorMock);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCObjectEventuallyTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    this.networkingDetectorMock.setConnected(true);
  }

  public void testSaveDummy() {
    LCObject object = new LCObject(CLASSNAME_STUDENT);
    LCQuery<LCObject> query = new LCQuery(CLASSNAME_STUDENT);
    try {
      int cntBefore = query.count();
      object.saveEventually();
      int cntAfter = query.count();
      System.out.println("cntBefore: " + cntBefore + ", cntAfter: " + cntAfter);
      assertEquals(cntAfter, cntBefore);
    } catch (LCException ex) {
      fail();
    }
  }

  public void testSaveDirectly() {
    LCQuery<LCObject> query = new LCQuery(CLASSNAME_STUDENT);
    int cntBefore = query.count();
    LCObject object = new LCObject(CLASSNAME_STUDENT);
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
    } catch (LCException ex) {
      ex.printStackTrace();
      fail();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
      fail();
    }
  }

  public void testSaveEventually() {
    LCQuery<LCObject> query = new LCQuery(CLASSNAME_STUDENT);
    int cntBefore = query.count();
    this.networkingDetectorMock.setConnected(false);
    LCObject object = new LCObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.add("course", "Art");
    object.put("occ", new Date());
    try {
      object.saveEventually();
      this.networkingDetectorMock.setConnected(true);
      Thread.sleep(2000);
      int cntAfter = query.count();
      System.out.println("cntBefore: " + cntBefore + ", cntAfter: " + cntAfter);
      assertEquals(cntAfter, cntBefore + 1);
    } catch (LCException ex) {
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
      Thread.sleep(2000);
    } catch (Exception ex) {
      fail();
    }
  }

  public void testDeleteInvalidObject() {
    LCObject object = new LCObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.deleteEventually();
  }

  public void testDeleteDirectly() {
    LCObject obj = LCObject.createWithoutData(CLASSNAME_STUDENT, "5b029e7fa22b9d0044bbef2a");
    obj.deleteEventually();
  }

  public void testDeleteEventually() {
    LCObject obj = LCObject.createWithoutData(CLASSNAME_STUDENT, "5b0285889f54542e39416143");
    this.networkingDetectorMock.setConnected(false);
    obj.deleteEventually();
    this.networkingDetectorMock.setConnected(true);
    try {
      Thread.sleep(2000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


}
