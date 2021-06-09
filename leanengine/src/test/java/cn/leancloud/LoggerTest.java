package cn.leancloud;

import cn.leancloud.core.LeanCloud;
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
    LeanCloud.setRegion(LeanCloud.REGION.NorthChina);
    LeanCloud.setLogLevel(LCLogger.Level.VERBOSE);
    LeanCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public void testFindObject() {
    LCQuery query = new LCQuery("Student");
    query.limit(4);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> o) {
        for(LCObject obj: o) {
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
