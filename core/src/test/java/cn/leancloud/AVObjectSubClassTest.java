package cn.leancloud;

import cn.leancloud.annotation.AVClassName;
import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

public class AVObjectSubClassTest extends TestCase {
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
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(AVObjectSubClassTest.class);
  }


  public void testSaveObject() {
    Student student = new Student();
    student.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println(avObject.toString());

      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testRefreshObject() {
    Student student = new Student();
    student.setObjectId("5a8e7d00128fe10037d2cf58");
    student.refreshInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println(avObject.toString());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testQuery() {
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
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
