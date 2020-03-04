package cn.leancloud;

import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;

public class AVCloudFunctionTest extends TestCase {
  private boolean testSucceed = false;

  public AVCloudFunctionTest(String name) {
    super(name);
    Configure.initializeRuntime();
    AVUser.logOut();
  }

  public static Test suite() {
    return new TestSuite(AVCloudFunctionTest.class);
  }

  public void testCloudFunction() {
//    String name = "joinLive";
//    Map<String, Object> param = new HashMap<String, Object>();
//    param.put("liveOld", "5e5cedca0a8a840067f6b0b8");
//    param.put("deviceCode", null);
//    param.put("version", "2140303121");
//    param.put("platform", "android");
//    Observable<JSONObject> res = AVCloud.callFunctionInBackground(name, param);
//    res.subscribe(new Observer<JSONObject>() {
//      @Override
//      public void onSubscribe(Disposable disposable) {
//
//      }
//
//      @Override
//      public void onNext(JSONObject jsonObject) {
//        System.out.println("结果 = " + jsonObject);
//      }
//
//      @Override
//      public void onError(Throwable throwable) {
//        System.out.println("error occurred! " + throwable);
//      }
//
//      @Override
//      public void onComplete() {
//
//      }
//    });
  }
}
