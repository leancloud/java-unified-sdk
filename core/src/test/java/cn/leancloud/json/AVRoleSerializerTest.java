package cn.leancloud.json;

import cn.leancloud.AVACL;
import cn.leancloud.AVRole;
import cn.leancloud.Configure;
import cn.leancloud.types.AVDate;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AVRoleSerializerTest extends TestCase {
  static {
    ConverterUtils.initialize();
  }
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
}
