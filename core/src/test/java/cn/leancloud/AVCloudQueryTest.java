package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.query.AVCloudQueryResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVCloudQueryTest extends TestCase {
  @AVClassName("Student")
  static class Student extends AVObject {
    public String getContent() {
      return getString("content");
    }
    public void setContent(String value) {
      put("content", value);
    }
  }

  public AVCloudQueryTest(String name) {
    super(name);
    AVObject.registerSubclass(Student.class);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite() {
    return new TestSuite(AVCloudQueryTest.class);
  }

  public void testQueryAVObject() {
    String cql = "select * from Post";
    AVCloudQuery.executeInBackground(cql, AVObject.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
          System.out.println(u.toString());
        }
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
  public void testQueryAVUser() {
    String cql = "select * from _User";
    AVCloudQuery.executeInBackground(cql, AVUser.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
          System.out.println(u.toString());
        }
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testQuerySubclass() {
    String cql = "select * from Student";
    AVCloudQuery.executeInBackground(cql, Student.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
          System.out.println(u.toString());
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
