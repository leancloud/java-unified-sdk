package cn.leancloud;

import cn.leancloud.types.LCGeoPoint;
import cn.leancloud.utils.LogUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LCObjectSerializerTest extends TestCase {
  private static final LCLogger LOGGER = LogUtil.getLogger(LCObjectSerializerTest.class);
  private static final String CLASSNAME_STUDENT = "Student";
  private static final String FILE_OBJECT_ID = "5e12b15dd4b56c007747193d";
  private String studentId = null;
  public LCObjectSerializerTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectSerializerTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    LCObject object = new LCObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("occ", new Date());
    String fileObjectId = FILE_OBJECT_ID;
    LCFile file = LCFile.withObjectIdInBackground(fileObjectId).blockingFirst();
    object.put("avatar", file);
    LCGeoPoint loc = new LCGeoPoint(89.4223, -45.43);
    object.put("location", loc);
    object.put("course", Arrays.asList("Music", "Science", "Math", "Computer"));
    object.save();
    studentId = object.getObjectId();
    LOGGER.d("[setUp] create new Student with objectId:" + studentId);
  }

  @Override
  protected void tearDown() throws Exception {
    LCObject object = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    object.delete();
    LOGGER.d("[tearDown] delete Student with objectId:" + studentId);
  }

  public void testPointerAttr() {
    // create new object
    LCObject object = new LCObject(CLASSNAME_STUDENT);
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.put("occ", new Date());
    LCObject leader = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    object.put("groupLead", leader);
    object.save();

    // fetch again.
    LCObject rst = LCObject.createWithoutData(CLASSNAME_STUDENT, object.getObjectId());
    rst.fetch();

    object.delete();

    // test attr.
    LCObject leaderRst = rst.getLCObject("groupLead");
    assertNotNull(leaderRst);
  }

  public void testAVFileAttr() {
    LCObject leader = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    leader.refresh("avatar");
    LCFile avatarFile = leader.getLCFile("avatar");
    assertNotNull(avatarFile);
  }

  public void testRelationAttr() {
    ;
  }

  public void testStringArrayAttr() {
    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    List<String> courses = s.getList("course");
    assertNotNull(courses);
    assertEquals(courses.size(), 4);
  }

  public void testObjectArrayAttr() {
    LCObject dad = LCObject.createWithoutData("Person", "5bff468944d904005f856849");
    LCObject mom = LCObject.createWithoutData("Person", "5bff46911579a3005f2207dd");
    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.put("parents", Arrays.asList(dad, mom));
    s.save();
    LCObject ag = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    ag.refresh();
    List<LCObject> parents = ag.getList("parents");
    assertEquals(parents.size(), 2);
  }

  public void testGEOPointAttr() {
    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    LCGeoPoint pt = s.getLCGeoPoint("location");
    assertNotNull(pt);
  }

  public void testDateAttr() {
    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    Date d = s.getDate("occ");
    assertNotNull(d);
  }

  public void testPrimitiveAttr() {
    // string, int,
    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    int age = s.getInt("age");
    String name = s.getString("name");
    assertEquals(age, 18);
    assertEquals(name, "Automatic Tester");
  }

  public void testDeserializer() {
    String oldVersionString = "{ \"@type\":\"com.example.avoscloud_demo.Student\",\"objectId\":\"5bff468944d904005f856849\",\"updatedAt\":\"2018-12-08T09:53:05.008Z\",\"createdAt\":\"2018-11-29T01:53:13.327Z\",\"className\":\"Student\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"name\":\"Automatic Tester's Dad\",\"course\":[\"Math\",\"Art\"],\"age\":20}}";
    LCObject oldV = LCObject.parseAVObject(oldVersionString);
    assertTrue((null != oldV) && oldV.getObjectId().length() > 0);
    assertTrue(oldV.getInt("age") == 20);

    LCObject s = LCObject.createWithoutData(CLASSNAME_STUDENT, studentId);
    s.refresh();
    LCObject newV = LCObject.parseAVObject(s.toJSONString());
    assertTrue((null != newV) && newV.getObjectId().length() > 0);
    LCObject newS = LCObject.parseAVObject(s.toJSONString());
    assertTrue((null != newS) && newS.getObjectId().length() > 0);

    LCObject c = new LCObject(CLASSNAME_STUDENT);
    c.put("name", "test");
    String archivedString = ArchivedRequests.getArchiveContent(c, false);
    LCObject newD = ArchivedRequests.parseAVObject(archivedString);
    assertTrue(null != newD);
  }
}
