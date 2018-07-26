package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AnonymousUtilTest extends TestCase {
  public AnonymousUtilTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AnonymousUtilTest.class);
  }

  public void testAnonymousLogin() {
    AVAnonymousUtils.logIn().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        System.out.println("anonymous user: " + avUser.toString());
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        fail();
      }

      @Override
      public void onComplete() {

      }
    });
  }
}
