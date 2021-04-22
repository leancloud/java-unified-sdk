package cn.leancloud;

import cn.leancloud.json.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LCPushTest extends TestCase {
  private boolean testSucceed;

  public LCPushTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public void testSimplePush() throws Exception {
    LCPush push = new LCPush();
    push.setMessage("test from unittest");
    push.send();
  }

  public void testIOSEnvironment() throws Exception {
    LCPush push = new LCPush();
    Map<String, Object> pushData = new HashMap<>();
    pushData.put("alert", "wmq2");
    pushData.put("body", "LeanCloud 发送测试2");
    push.setPushToIOS(true);
    push.setPushToAndroid(false);
    push.setData(pushData);
    push.setFlowControl( 200);
    push.setChannel("03fc00e69bea4da98a5fbadb2432a53f");
    push.setiOSEnvironment(LCPush.iOSEnvironmentDev);

    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;

    push.sendInBackground().subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println("send succeed. " + jsonObject);
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testPushFlowControl() throws Exception {
    LCPush push = new LCPush();
    Map<String, Object> pushData = new HashMap<>();
    pushData.put("alert", "push message to android device directly");
    push.setPushToAndroid(true);
    push.setData(pushData);
    push.setFlowControl( 200);
    assertEquals(push.getFlowControl(), 1000);

    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    push.sendInBackground().subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable d) {
      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println("推送成功" + jsonObject);
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        System.out.println("推送失败，错误信息：" + e.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testPushTargetWithData() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    LCPush push = new LCPush();
    Map<String, Object> pushData = new HashMap<>();
    pushData.put("alert", "push message to android device directly");
    push.setPushToAndroid(true);
    push.setData(pushData);
    push.sendInBackground().subscribe(new Observer<JSONObject>() {
      @Override
      public void onSubscribe(Disposable d) {
      }

      @Override
      public void onNext(JSONObject jsonObject) {
        System.out.println("推送成功" + jsonObject);
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        System.out.println("推送失败，错误信息：" + e.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
