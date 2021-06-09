package cn.leancloud.ops;

import cn.leancloud.LCObject;
import cn.leancloud.Configure;
import cn.leancloud.LCACL;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.json.JSONObject;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import junit.framework.TestCase;

import java.util.Map;

public class SetOperationTests extends TestCase {
  public SetOperationTests(String testName) {
    super(testName);
    AppConfiguration.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    Configure.initializeRuntime();
  }

  public void testSetACL() {
    LCACL acl = new LCACL();
    acl.setPublicReadAccess(true);
    acl.setPublicWriteAccess(false);
    System.out.println(acl.toJSONObject().toJSONString());

    SetOperation op = (SetOperation) OperationBuilder.gBuilder.create(
            OperationBuilder.OperationType.Set, "ACL", acl);
    Map<String, Object> result = op.encode();
    System.out.println(result.toString());
    assertNotNull(result);
  }

  public void testSetObject() {
    LCObject p = new LCObject("Student");
    p.setObjectId("fewruwpr");

    SetOperation op = (SetOperation) OperationBuilder.gBuilder.create(
            OperationBuilder.OperationType.Set, "friend", p);
    Map<String, Object> result = op.encode();
    System.out.println(JSONObject.Builder.create(result).toJSONString());
  }
}
