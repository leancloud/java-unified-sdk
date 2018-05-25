package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.core.AppConfiguration;
import cn.leancloud.logging.Log4jAdapter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.TestCase;

import java.util.List;

public class LoggerTest extends TestCase {
  public LoggerTest(String name) {
    super(name);
    AppConfiguration.setLogAdapter(new Log4jAdapter());
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testFindObject() {
    AVQuery query = new AVQuery("Student");
    query.limit(4);
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<AVObject> o) {
        for(AVObject obj: o) {
          System.out.println("Query of Student is: " + obj.toString());
        }
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
