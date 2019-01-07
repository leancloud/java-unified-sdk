package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVObjectSubClassTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  @AVClassName("Student")
  static class Student extends AVObject {
    public String getContent() {
      return getString("content");
    }
    public void setContent(String value) {
      put("content", value);
    }
  }

  public AVObjectSubClassTest(String testName) {
    super(testName);
    AVObject.registerSubclass(Student.class);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectSubClassTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testSaveObject() throws Exception{
    Student student = new Student();
    student.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println(avObject.toString());
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

  public void testRefreshObject() throws Exception {
    Student student = new Student();
    student.setObjectId("5a8e7d00128fe10037d2cf58");
    student.refreshInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println(avObject.toString());
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

  public void testQuery() throws Exception {
    AVQuery<Student> query = AVQuery.getQuery(Student.class);
    query.whereGreaterThan("age", 18);
    query.whereDoesNotExist("name");
    query.findInBackground().subscribe(new Observer<List<Student>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<Student> students) {
        for (Student s: students) {
          System.out.println(s.toString());
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
