package cn.leancloud.ops;

import cn.leancloud.AVObject;
import cn.leancloud.Configure;
import cn.leancloud.annotation.AVClassName;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.core.PaasClient;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import junit.framework.TestCase;

import java.util.Map;

public class AddOperationTests extends TestCase {
  @AVClassName("Student")
  private static class Student extends AVObject {
    ;
  }
  public AddOperationTests(String testName) {
    super(testName);
    AppConfiguration.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    Configure.initializeRuntime();
  }

  public void testAddObject() {
    Student s = new Student();
    s.setObjectId("StudentObjectId");
    AddOperation op = (AddOperation) OperationBuilder.gBuilder.create(
            OperationBuilder.OperationType.Add, "classmate", s);
    Map<String, Object> result = op.encode();
    System.out.println(new JSONObject(result).toJSONString());
  }
}
