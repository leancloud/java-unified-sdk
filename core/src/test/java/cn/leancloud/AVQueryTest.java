package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class AVQueryTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  private String currentObjectId = null;
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
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.save();
    currentObjectId = object.getObjectId();
    AVUser.changeCurrentUser(null, true);
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
    AVObject object = new AVObject("Student");
    object.setObjectId(currentObjectId);
    object.delete();
  }

  public void testFetchSingleObject() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.getInBackground(currentObjectId).subscribe(new Observer<AVObject>() {
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

  public void testCountThenFind() throws Exception {
    AVQuery query = new AVQuery("Student");
    int cnt = query.count();
    assertTrue(cnt > 0);
    AVObject firstObj = query.getFirst();
    assertTrue(null != firstObj);
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVObject> o) {
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

  public void testClone() throws Exception {
    AVQuery query = new AVQuery("Student");
    query.orderByDescending(AVObject.KEY_CREATED_AT);
    AVQuery q1 = query.clone();
    assertEquals(q1.getCachePolicy(), query.getCachePolicy());
    assertEquals(q1.getClassName(), query.getClassName());
    assertEquals(q1.getClazz(), query.getClazz());
    assertEquals(q1.getClass(), query.getClass());
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
        System.out.println("completed.");
        latch.countDown();
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
    query.skip(1);
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

  public void testDeepIncludeQuery() throws Exception {
    AVQuery<AVObject> queryLikeRed = new AVQuery<>("hb_Praise");
    queryLikeRed.include("taskId");
    queryLikeRed.include("taskId.userId");
    queryLikeRed.selectKeys(Arrays.asList("taskId.userId.nickName", "taskId.userId.username", "createdAt"));
    queryLikeRed.whereEqualTo("userId", AVObject.createWithoutData("_User", "userobjectid000000100008"));
    int total = queryLikeRed.count();
    List<AVObject> queryList = queryLikeRed.find();
    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> redList = new ArrayList<>();
    for (AVObject redPacket: queryList) {
      Map<String, Object> save = new HashMap<>();
      AVObject task = redPacket.getAVObject("taskId");
      System.out.println(task);
      AVUser user = task.getAVObject("userId");
      System.out.println(user);
    }

  }
}
