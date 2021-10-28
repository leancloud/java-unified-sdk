package cn.leancloud;

import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Test;

@RunWith(MockitoJUnitRunner.class)
public class AVObjectTest {
  public AVObjectTest() {
    LeanCloud.setRegion(LeanCloud.REGION.NorthChina);
    LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
    LeanCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  protected void setUp() throws Exception {
    ;
  }

  @Test
  public void testCreateObject() throws Exception {
    System.out.println("enter testCreateObject()");
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(LCObject avObject) {
        System.out.println("create object finished. " + avObject.toString());
        avObject.deleteInBackground().subscribe(new Observer<LCNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(LCNull aVoid) {
            System.out.println("delete object finished.");
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed.");
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("create object failed.");
      }

      public void onComplete() {
      }
    });
    Thread.sleep(3000);
    System.out.println("exit testCreateObject()");
  }

}