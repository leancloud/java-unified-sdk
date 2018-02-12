package cn.leancloud.core;

import cn.leancloud.Configure;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVObjectTest extends TestCase {
  public AVObjectTest(String testName) {
    super(testName);
  }
  public static Test suite() {
    return new TestSuite(AVObjectTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testObjectRefresh() {
    AVObject object = new AVObject("Student");
    object.setObjectId("5a7a4ac8128fe1003768d2b1");
    object.refreshInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("subscribe result: " + avObject.toString());
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }

      public void onComplete() {

      }
    });
    try {
      Thread.sleep(2000);
      System.out.println("self result: " + object.toString());
    }catch (Exception ex) {
      fail();
    }
  }
}
