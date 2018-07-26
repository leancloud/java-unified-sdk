package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVGeoPoint;
import cn.leancloud.utils.LogUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AVObjectSerializerTest extends TestCase {
  private static final AVLogger LOGGER = LogUtil.getLogger(AVObjectSerializerTest.class);
  private static final String CLASSNAME_STUDENT = "Student";
  private String studentId = null;
  public AVObjectSerializerTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectSerializerTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("occ", new Date());
    String fileObjectId = "5aa634357565710044bde4df";
    AVFile file = AVFile.withObjectIdInBackground(fileObjectId).blockingFirst();
    object.put("avatar", file);
    AVGeoPoint loc = new AVGeoPoint(89.4223, -45.43);
    object.put("location", loc);
    object.put("course", Arrays.asList("Music", "Science", "Math", "Computer"));
    object.save();
    studentId = object.getObjectId();
    LOGGER.d("[setUp] create new Student with objectId:" + studentId);
  }

  @Override
  protected void tearDown() throws Exception {
    AVObject object = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    object.delete();
    LOGGER.d("[tearDown] delete Student with objectId:" + studentId);
  }

  public void testPointerAttr() {
    // create new object
    AVObject object = new AVObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("occ", new Date());
    AVObject leader = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    object.put("groupLead", leader);
    object.save();

    // fetch again.
    AVObject rst = AVObject.createWithoutData(CLASSNAME_STUDENT, object.getObjectId());
    rst.fetch();

    object.delete();

    // test attr.
    AVObject leaderRst = rst.getAVObject("groupLead");
    assertNotNull(leaderRst);
  }

  public void testAVFileAttr() {
    AVObject leader = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    leader.refresh("avatar");
    AVFile avatarFile = leader.getAVFile("avatar");
    assertNotNull(avatarFile);
  }

  public void testRelationAttr() {
    ;
  }

  public void testStringArrayAttr() {
    AVObject s = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    List<String> courses = s.getList("course");
    assertNotNull(courses);
    assertEquals(courses.size(), 4);
  }

  public void testObjectArrayAttr() {
    AVObject dad = AVObject.createWithoutData("Person", "5ad88466ee920a3f733c306f");
    AVObject mom = AVObject.createWithoutData("Person", "5ad884661b69e600670e6cf3");
    AVObject s = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.put("parents", Arrays.asList(dad, mom));
    s.save();
    AVObject ag = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    ag.refresh();
    List<AVObject> parents = ag.getList("parents");
    assertEquals(parents.size(), 2);
  }

  public void testGEOPointAttr() {
    AVObject s = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    AVGeoPoint pt = s.getAVGeoPoint("location");
    assertNotNull(pt);
  }

  public void testDateAttr() {
    AVObject s = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    Date d = s.getDate("occ");
    assertNotNull(d);
  }

  public void testPrimitiveAttr() {
    // string, int,
    AVObject s = AVObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    int age = s.getInt("age");
    String name = s.getString("name");
    assertEquals(age, 18);
    assertEquals(name, "Automatic Tester");
  }
}
