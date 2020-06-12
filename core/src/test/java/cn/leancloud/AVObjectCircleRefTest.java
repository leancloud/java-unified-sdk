package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVObjectCircleRefTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVObjectCircleRefTest(String testName) {
    super(testName);
    AVObject.registerSubclass(AVObjectSubClassTest.Student.class);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectCircleRefTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testSimpleCircleRef() throws Exception {
    AVObject student = new AVObjectSubClassTest.Student();
    AVObject teacher = new AVObject("Teacher");
    teacher.put("student", student);
    student.put("teacher", teacher);
    teacher.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        testSucceed = throwable.getMessage().indexOf("Found a circular dependency when saving") > -1;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDeeperCircleRef() throws Exception {
    AVObject studentA = new AVObjectSubClassTest.Student();
    AVObject studentB = new AVObjectSubClassTest.Student();

    studentA.put("friend", studentB);

    AVObject teacher = new AVObject("Teacher");
    teacher.put("student", studentA);
    studentB.put("teacher", teacher);

    teacher.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        testSucceed = throwable.getMessage().indexOf("Found a circular dependency when saving") > -1;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCollectionValueCircleRef() throws Exception {
    AVObject studentA = new AVObjectSubClassTest.Student();
    AVObject studentB = new AVObjectSubClassTest.Student();

    AVObject teacher = new AVObject("Teacher");
    studentB.put("teacher", teacher);

    List<AVObject> students = new ArrayList<>(2);
    students.add(studentA);
    students.add(studentB);

    teacher.addAll("student", students);

    teacher.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        testSucceed = throwable.getMessage().indexOf("Found a circular dependency when saving") > -1;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testComplexCircleRef() throws Exception {
    // studentA
    // studentB -> Teacher
    // studentC ->(friend) [studentA, studentB]
    // teatcher -> studentC

    AVObject studentA = new AVObjectSubClassTest.Student();
    AVObject studentB = new AVObjectSubClassTest.Student();
    AVObject studentC = new AVObjectSubClassTest.Student();

    AVObject teacher = new AVObject("Teacher");
    studentB.put("teacher", teacher);

    List<AVObject> students = new ArrayList<>(2);
    students.add(studentA);
    students.add(studentB);
    studentC.addAllUnique("friends", students);

    teacher.put("mostLike", studentC);

    teacher.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        testSucceed = throwable.getMessage().indexOf("Found a circular dependency when saving") > -1;
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
