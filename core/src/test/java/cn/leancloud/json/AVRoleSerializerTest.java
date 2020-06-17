package cn.leancloud.json;

import cn.leancloud.AVACL;
import cn.leancloud.AVObject;
import cn.leancloud.AVRole;
import cn.leancloud.Configure;
import cn.leancloud.gson.ConverterUtils;
import cn.leancloud.ops.AddOperation;
import cn.leancloud.types.AVDate;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AVRoleSerializerTest extends TestCase {

  public AVRoleSerializerTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public void testRoleSerializer() {
    AVACL acl = new AVACL();
    acl.setPublicReadAccess(true);
    acl.setPublicWriteAccess(false);
    acl.setRoleReadAccess("Tester", true);
    acl.setRoleWriteAccess("Tester", true);
    String aclString = JSON.toJSONString(acl);
    System.out.println("jsonString of acl:" + aclString);
    AVACL otherACL = JSON.parseObject(aclString, AVACL.class);
    System.out.println("jsonString of acl:" + aclString + ", deserializedObject: " + otherACL.toString());
    assertEquals(acl, otherACL);

    AVRole role1 = new AVRole();
    AVRole role2 = new AVRole("Developer");
    HashMap<String, Boolean> aclData = new HashMap<>();
    aclData.put("read", true);
    aclData.put("write", true);
    AVRole role3 = new AVRole("Ops", new AVACL(aclData));

    List<AVRole> roleList = new ArrayList<>(3);
    roleList.add(role1);
    roleList.add(role2);
    roleList.add(role3);

    for (AVRole role : roleList) {
      String jsonString = JSON.toJSONString(role);
      System.out.println("jsonString of role:" + jsonString);
      AVRole dup = JSON.parseObject(jsonString, AVRole.class);
      System.out.println("deserializedObject: " + dup.toJSONString());
      assertEquals(role.getName(), dup.getName());
    }
  }

  public void testAVDate() {
    AVDate date = new AVDate();
    date.setIso("2020-06-06'T'00:00:00.533'Z'");
    String dateJson = date.toJSONString();
    System.out.println("jsonString of AVDate:" + dateJson);
    AVDate other = JSON.parseObject(dateJson, AVDate.class);
    System.out.println("deserializedObject: " + other.toJSONString());
    assertEquals(other.getIso().equals(date.getIso()), true);
  }

  public void testAVObjectSerializer() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    String objectString = object.toJSONString();
    System.out.println("objectJSONString is: " + objectString);
  }

  public void testAVObjectDeserialize() {
    String text = "{\"@type\":\"cn.leancloud.AVObject\",\"className\":\"Student\",\"version\":5,\"serverData\":{\"address\":\"Beijing City\",\"@type\":\"java.util.HashMap\",\"age\":5}}";
    AVObject object = ConverterUtils.parseObject(text, AVObject.class);
    System.out.println(object.toJSONString());
  }

  public void testAVUserSerializer() {

  }

  public void testSubClassSerializer() {
    ;
  }

  public void testBaseOperatorSerializer() {
    AddOperation op = new AddOperation("age", 5);
    System.out.println("Operator jsonString is: " + ConverterUtils.getGsonInstance().toJson(op));
  }
}
