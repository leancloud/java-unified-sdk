package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.network.PaasClient;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVObjectAsyncTest extends TestCase {
  public AVObjectAsyncTest(String testName) {
    super(testName);
    PaasClient.config(true, new PaasClient.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(AVObjectAsyncTest.class);
  }

  @Override
  protected void setUp() throws Exception {
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
        fail();
      }

      public void onComplete() {
        System.out.println("subscribe completed.");
      }
    });
    System.out.println("wait response...");
    try {
      Thread.sleep(2000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("test completed.");
  }
}
