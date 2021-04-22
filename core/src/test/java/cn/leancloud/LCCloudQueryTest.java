package cn.leancloud;

import cn.leancloud.annotation.LCClassName;
import cn.leancloud.query.LCCloudQueryResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class LCCloudQueryTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

  @LCClassName("Student")
  static class Student extends LCObject {
    public String getContent() {
      return getString("content");
    }
    public void setContent(String value) {
      put("content", value);
    }
  }

  public LCCloudQueryTest(String name) {
    super(name);
    LCObject.registerSubclass(Student.class);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCCloudQueryTest.class);
  }

  public void testQueryAVObject() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);

    String cql = "select * from Post";
    LCCloudQuery.executeInBackground(cql, LCObject.class).subscribe(new Observer<LCCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCCloudQueryResult LCCloudQueryResult) {
        for (LCObject u: LCCloudQueryResult.getResults()) {
          System.out.println(u.toString());
        }
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
  public void testQueryAVUser() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);

    String cql = "select * from _User";
    LCCloudQuery.executeInBackground(cql, LCUser.class).subscribe(new Observer<LCCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCCloudQueryResult LCCloudQueryResult) {
        for (LCObject u: LCCloudQueryResult.getResults()) {
          System.out.println(u.toString());
        }
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQuerySubclass() throws  Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);

    String cql = "select * from Student";
    LCCloudQuery.executeInBackground(cql, Student.class).subscribe(new Observer<LCCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCCloudQueryResult LCCloudQueryResult) {
        for (LCObject u: LCCloudQueryResult.getResults()) {
          System.out.println(u.toString());
        }
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
