package cn.leancloud.core.ops;

import cn.leancloud.Configure;
import cn.leancloud.core.AVACL;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.network.PaasClient;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import junit.framework.TestCase;

import java.util.Map;

public class SetOperationTests extends TestCase {
  public SetOperationTests(String testName) {
    super(testName);
    PaasClient.config(true, new PaasClient.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testSetACL() {
    AVACL acl = new AVACL();
    acl.setPublicReadAccess(true);
    acl.setPublicWriteAccess(false);
    System.out.println(acl.toJSONObject().toJSONString());

    SetOperation op = (SetOperation) OperationBuilder.BUILDER.create(
            OperationBuilder.OperationType.Set, "ACL", acl);
    Map<String, Object> result = op.encode();
    System.out.println(result.toString());
    assertNotNull(result);
  }
}
