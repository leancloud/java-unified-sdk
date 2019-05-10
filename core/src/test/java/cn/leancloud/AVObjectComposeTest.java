package cn.leancloud;

import cn.leancloud.types.AVGeoPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVObjectComposeTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVObjectComposeTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVObjectComposeTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testGeoPointAttr() throws Exception {
    AVObject course = new AVObject("Course");
    course.put("name", "Math");
    course.put("location", new AVGeoPoint(45.9, 76.3));
    course.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        avObject.delete();
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testPointerAttr() throws Exception {
    AVObject course = new AVObject("Course");
    course.put("name", "Science");
    course.save();

    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);

    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("birthday", new Date());
    object.put("favorite", course);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVObject avObject) {
        AVQuery query = new AVQuery("Student");
        query.include("favorite");
        List<AVObject> result = query.find();
        testSucceed = result.size() > 0;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
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
