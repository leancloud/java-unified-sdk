package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCObjectCircleRefTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public LCObjectCircleRefTest(String testName) {
    super(testName);
    LCObject.registerSubclass(LCObjectSubClassTest.Student.class);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectCircleRefTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testSimpleCircleRef() throws Exception {
    LCObject student = new LCObjectSubClassTest.Student();
    LCObject teacher = new LCObject("Teacher");
    teacher.put("student", student);
    student.put("teacher", teacher);
    teacher.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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
    LCObject studentA = new LCObjectSubClassTest.Student();
    LCObject studentB = new LCObjectSubClassTest.Student();

    studentA.put("friend", studentB);

    LCObject teacher = new LCObject("Teacher");
    teacher.put("student", studentA);
    studentB.put("teacher", teacher);

    teacher.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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
    LCObject studentA = new LCObjectSubClassTest.Student();
    LCObject studentB = new LCObjectSubClassTest.Student();

    LCObject teacher = new LCObject("Teacher");
    studentB.put("teacher", teacher);

    List<LCObject> students = new ArrayList<>(2);
    students.add(studentA);
    students.add(studentB);

    teacher.addAll("student", students);

    teacher.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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

    LCObject studentA = new LCObjectSubClassTest.Student();
    LCObject studentB = new LCObjectSubClassTest.Student();
    LCObject studentC = new LCObjectSubClassTest.Student();

    LCObject teacher = new LCObject("Teacher");
    studentB.put("teacher", teacher);

    List<LCObject> students = new ArrayList<>(2);
    students.add(studentA);
    students.add(studentB);
    studentC.addAllUnique("friends", students);

    teacher.put("mostLike", studentC);

    teacher.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject LCObject) {
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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
