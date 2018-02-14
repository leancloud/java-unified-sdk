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
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(AVObjectTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testCreateObject() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(AVObject avObject) {
        System.out.println("create object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
//        avObject.deleteInBackground().subscribe(new Observer<Void>() {
//          public void onSubscribe(Disposable disposable) {
//            ;
//          }
//
//          public void onNext(Void aVoid) {
//            System.out.println("delete object finished.");
//          }
//
//          public void onError(Throwable throwable) {
//            System.out.println("delete object failed.");
//          }
//
//          public void onComplete() {
//          }
//        });

      }

      public void onError(Throwable throwable) {
        System.out.println("create object failed.");

      }

      public void onComplete() {
      }
    });
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
        fail();
      }

      public void onComplete() {
        System.out.println("subscribe completed.");
      }
    });
    System.out.println("test completed.");
  }
}
