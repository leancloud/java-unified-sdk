package cn.leancloud.ops;

import cn.leancloud.AVObject;
import cn.leancloud.Configure;
import cn.leancloud.AVACL;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.PaasClient;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import junit.framework.TestCase;

import java.util.Map;

public class SetOperationTests extends TestCase {
  public SetOperationTests(String testName) {
    super(testName);
    PaasClient.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    Configure.initializeRuntime();
  }

  public void testSetACL() {
    AVACL acl = new AVACL();
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
    AVObject p = new AVObject("Student");
    p.setObjectId("fewruwpr");

    SetOperation op = (SetOperation) OperationBuilder.gBuilder.create(
            OperationBuilder.OperationType.Set, "friend", p);
    Map<String, Object> result = op.encode();
    System.out.println(new JSONObject(result).toJSONString());
  }
}
