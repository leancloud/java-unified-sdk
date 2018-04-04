package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

public class AVQueryTest extends TestCase {
  public AVQueryTest(String testName) {
    super(testName);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(AVQueryTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testFetchSingleObject() {
    AVQuery query = new AVQuery("Student");
    query.getInBackground("5a8e7e0efe88c200388bc8f0").subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject o) {
        System.out.println(o.toString());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testGenericQuery() {
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
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testGenericCount() {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.countInBackground().subscribe(new Observer<Integer>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Integer o) {
        System.out.println("Count of Student is:" + o.intValue());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testFirstQuery() {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.getFirstInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject o) {
        System.out.println(o.toString());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testQueryCacheNotExisted() {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    query.limit(5);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<AVObject> o) {
        fail();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
      }

      public void onComplete() {

      }
    });
  }

  public void testQueryCacheElseNetworking() {
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
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
