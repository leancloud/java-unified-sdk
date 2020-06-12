package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.query.AVCloudQueryResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class AVCloudQueryTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

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
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVCloudQueryTest.class);
  }

  public void testQueryAVObject() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);

    String cql = "select * from Post";
    AVCloudQuery.executeInBackground(cql, AVObject.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
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
    AVCloudQuery.executeInBackground(cql, AVUser.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
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
    AVCloudQuery.executeInBackground(cql, Student.class).subscribe(new Observer<AVCloudQueryResult>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVCloudQueryResult avCloudQueryResult) {
        for (AVObject u: avCloudQueryResult.getResults()) {
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
