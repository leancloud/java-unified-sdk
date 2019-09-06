package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Map;

public class AVCloudTest extends TestCase {
  public AVCloudTest(String name) {
    super(name);
    Configure.initializeRuntime();

  }

  public static Test suite() {
    return new TestSuite(AVCloudTest.class);
  }

  public void testCloudFunction() {
//    String name = "hallo";
//    Map<String, Object> param = new HashMap<String, Object>();
//    Observable<JSONObject> res = AVCloud.callFunctionInBackground(name, param);
//    res.blockingSubscribe();
  }

  public void testCloudRPC() {
    String name = "leanengine/update-leanengine-function-metadata";
    Map<String, String> param = new HashMap<String, String>();
    param.put("content", "test");
    Object res = AVCloud.callRPCInBackground(name, param).blockingFirst();
    assertNotNull(res);
  }
}
