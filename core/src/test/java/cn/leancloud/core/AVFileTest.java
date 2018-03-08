package cn.leancloud.core;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.network.PaasClient;
import cn.leancloud.utils.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVFileTest extends TestCase {
  public AVFileTest(String name) {
    super(name);
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
    return new TestSuite(AVFileTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testUploader() {
    String contents = StringUtil.getRandomString(64);
    AVFile file = new AVFile("test", contents.getBytes());
    Observable<AVFile> result = file.saveInBackground();
    result.subscribe(new Consumer<AVFile>() {
      public void accept(AVFile avFile) throws Exception {
        if (null == avFile) {
          fail();
        } else {
          System.out.println("succeed to upload file. objectId=" + avFile.getObjectId());
          avFile.deleteInBackground().subscribe(new Observer<Void>() {
            public void onSubscribe(Disposable disposable) {

            }

            public void onNext(Void aVoid) {
              System.out.println("succeed to delete file.");
            }

            public void onError(Throwable throwable) {
              throwable.printStackTrace();
              fail();
            }

            public void onComplete() {

            }
          });
        }
      }
    });
  }
}
