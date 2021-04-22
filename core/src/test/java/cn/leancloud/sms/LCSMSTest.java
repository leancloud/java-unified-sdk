package cn.leancloud.sms;

import cn.leancloud.Configure;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class LCSMSTest extends TestCase {
  private boolean testSuccess;
  public LCSMSTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCSMSTest.class);
  }

  public void testNormalMobilePhone() throws Exception {
    LCSMSOption option = new LCSMSOption();
    option.setApplicationName("LeanCloud");
    option.setOperation("Register");
    option.setTemplateName("my Template");
    option.setSignatureName("LeanCloud");

    final CountDownLatch latch = new CountDownLatch(1);
    testSuccess = false;
    LCSMS.requestSMSCodeInBackground("18600345198", option).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
        testSuccess = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        testSuccess = true;
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
    LCSMS.requestSMSCodeInBackground("218600345198", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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

    LCSMS.requestSMSCodeInBackground("1+8600345198", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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

    LCSMS.requestSMSCodeInBackground("+18600345198", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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

    LCSMS.requestSMSCodeInBackground("28600345198", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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

    LCSMS.requestSMSCodeInBackground("+85290337941", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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

    LCSMS.requestSMSCodeInBackground("+8619334290337941", null).subscribe(new Observer<LCNull>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCNull LCNull) {
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
