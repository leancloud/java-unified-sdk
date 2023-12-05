package cn.leancloud.core;

import cn.leancloud.Configure;
import cn.leancloud.types.LCDate;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CountDownLatch;

public class LeanCloudTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LeanCloudTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testNetworkTimeout() throws Exception {
	  PaasClient.cleanup();
	  LeanCloud.setNetworkTimeout(10); // recover to default value.
    Configure.initializeRuntime();
	  OkHttpClient okhttpClient = PaasClient.getGlobalOkHttpClient();

	  Request request = new Request.Builder().url("https://httpbin.org/delay/15").build();
	  try {
	      okhttpClient.newCall(request).execute();
	  } catch (Exception ex) {
		  System.out.println("failed to get response, due to time-out");
		  ex.printStackTrace();
		  testSucceed = true;
	  }
	  assertTrue(testSucceed);

	  request = new Request.Builder().url("https://httpbin.org/delay/5").build();
	  try {
		  Response response = okhttpClient.newCall(request).execute();
	      System.out.println("Succeed to get response...." + response.body().string());
		  testSucceed = true;
	  } catch (Exception ex) {
		  ex.printStackTrace();
		  testSucceed = false;
	  }
	  assertTrue(testSucceed);
  }

  public void testNetworkTimeoutWithSpecifiedValue() throws Exception {
	  PaasClient.cleanup();
	  LeanCloud.setNetworkTimeout(6);
	  Configure.initializeRuntime();
	  OkHttpClient okhttpClient = PaasClient.getGlobalOkHttpClient();

	  Request request = new Request.Builder().url("https://httpbin.org/delay/8").build();
	  try {
		  Response response = okhttpClient.newCall(request).execute();
	      System.out.println("Succeed to get response...." + response.body().string());
	  } catch (Exception ex) {
		  System.out.println("failed to get response, due to time-out");
		  ex.printStackTrace();
		  testSucceed = true;
	  }
	  assertTrue(testSucceed);

	  request = new Request.Builder().url("https://httpbin.org/delay/5").build();
	  try {
		  Response response = okhttpClient.newCall(request).execute();
	      System.out.println("Succeed to get response...." + response.body().string());
		  testSucceed = true;
	  } catch (Exception ex) {
		  ex.printStackTrace();
		  testSucceed = false;
	  }
	  assertTrue(testSucceed);
  }

  public void testServerDate() throws Exception {
	  PaasClient.cleanup();
	  LeanCloud.setNetworkTimeout(10); // recover to default value.
    Configure.initializeRuntime();
    LeanCloud.getServerDateInBackground().subscribe(new Observer<LCDate>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCDate LCDate) {
        testSucceed = true;
        System.out.println(LCDate.toJSONString());
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
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
