package cn.leancloud.sms;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class AVSMSTest extends TestCase {
  private boolean testSuccess;
  public AVSMSTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVSMSTest.class);
  }

  public void testNormalMobilePhone() throws Exception {
    AVSMSOption option = new AVSMSOption();
    option.setApplicationName("LeanCloud");
    option.setOperation("Register");
    option.setTemplateName("my Template");
    option.setSignatureName("LeanCloud");

    final CountDownLatch latch = new CountDownLatch(1);
    testSuccess = false;
    AVSMS.requestSMSCodeInBackground("18600345198", option).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        testSuccess = true;
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
    assertTrue(testSuccess);
  }

  public void testTooLongMobilePhone() {
    AVSMS.requestSMSCodeInBackground("218600345198", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("mobile phone number is empty or invalid"));
      }

      @Override
      public void onComplete() {

      }
    });

    AVSMS.requestSMSCodeInBackground("1+8600345198", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("mobile phone number is empty or invalid"));
      }

      @Override
      public void onComplete() {

      }
    });

    AVSMS.requestSMSCodeInBackground("+18600345198", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("smsOption is null"));
      }

      @Override
      public void onComplete() {

      }
    });

    AVSMS.requestSMSCodeInBackground("28600345198", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("mobile phone number is empty or invalid"));
      }

      @Override
      public void onComplete() {

      }
    });
  }

  public void testInternationalMobile() {

    AVSMS.requestSMSCodeInBackground("+85290337941", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("smsOption is null"));

      }

      @Override
      public void onComplete() {

      }
    });

    AVSMS.requestSMSCodeInBackground("+8619334290337941", null).subscribe(new Observer<AVNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVNull avNull) {
        fail();
      }

      @Override
      public void onError(Throwable throwable) {
        assertTrue(throwable.getMessage().equalsIgnoreCase("smsOption is null"));

      }

      @Override
      public void onComplete() {

      }
    });
  }
}
