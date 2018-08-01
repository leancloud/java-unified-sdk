package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVQueryTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  public AVQueryTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVQueryTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
  }

  public void testFetchSingleObject() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.getInBackground("5a8e7e0efe88c200388bc8f0").subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject o) {
        System.out.println(o.toString());
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

  public void testGenericQuery() throws Exception {
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

  public void testGenericCount() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.countInBackground().subscribe(new Observer<Integer>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Integer o) {
        System.out.println("Count of Student is:" + o.intValue());
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

  public void testFirstQuery() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.getFirstInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject o) {
        System.out.println(o.toString());
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

  public void testQueryCacheNotExisted() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.limit(5);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<AVObject> o) {
        System.out.println(o.toString());
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQueryCacheElseNetworking() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.limit(5);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<AVObject> o) {
        for (AVObject j: o) {
          System.out.println("found result: " + j.toString());
        }
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
