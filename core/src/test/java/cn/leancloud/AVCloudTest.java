package cn.leancloud;

import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

public class AVCloudTest extends TestCase {
  public AVCloudTest(String name) {
    super(name);
    Configure.initializeRuntime();
    AVUser.logOut();
  }

  public static Test suite() {
    return new TestSuite(AVCloudTest.class);
  }

  public void testCloudFunction() {
    String name = "hallo";
    Map<String, Object> param = new HashMap<String, Object>();
    Observable<JSONObject> res = AVCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println("结果 = " + jsonObject);
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error occurred! " + throwable);
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testCloudFunction4IMPresence() {
    String name = "getOnOffStatus";
    Map<String, Object> param = new HashMap<String, Object>();
    List<String> userIds = Arrays.asList("4B7FFEF52D744A07A2A85335F34402D7", "5de4e42321b47e006ca18ffe");
    param.put("peerIds", userIds);
    Observable<List<String>> res = AVCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(List<String> strings) {
                      System.out.println("结果 = " + strings);
                      for (String t : strings) {
                        if (null == t) {
                          System.out.println("result: unknown");
                        } else if (1 == Integer.valueOf(t)) {
                          System.out.println("result: online");
                        } else {
                          System.out.println("result: offline");
                        }
                      }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                      System.out.println("error occurred! " + throwable);
                    }

                    @Override
                    public void onComplete() {

                    }
                  });
  }

  public void testCloudRPC() {
//    String name = "leanengine/update-leanengine-function-metadata";
//    Map<String, String> param = new HashMap<String, String>();
//    param.put("content", "test");
//    Object res = AVCloud.callRPCInBackground(name, param).blockingFirst();
//    assertNotNull(res);
  }
}
