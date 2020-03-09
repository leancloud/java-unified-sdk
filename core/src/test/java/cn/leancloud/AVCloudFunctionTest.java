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
    String name = "currentTime";
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<Long> res = AVCloud.callFunctionInBackground(name, param);
    res.subscribe(new Observer<Long>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(Long jsonObject) {
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
  public void testCloudFunctionWithCache() {
    String name = "currentTime";
    Map<String, Object> param = new HashMap<String, Object>();
    param.put("platform", "android");
    Observable<Long> res = AVCloud.callFunctionWithCacheInBackground(name, param, AVQuery.CachePolicy.CACHE_ELSE_NETWORK, 30000);
    res.subscribe(new Observer<Long>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(Long jsonObject) {
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
}
