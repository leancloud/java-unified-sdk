package cn.leancloud;

import cn.leancloud.callback.FindCallback;
import cn.leancloud.callback.GetCallback;
import cn.leancloud.convertor.ObserverBuilder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LCQueryTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;
  private String currentObjectId = null;
  public LCQueryTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCQueryTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", null);
    object.save();
    currentObjectId = object.getObjectId();
    LCUser.changeCurrentUser(null, true);
  }

  @Override
  protected void tearDown() throws Exception {
    latch = null;
    LCObject object = new LCObject("Student");
    object.setObjectId(currentObjectId);
    object.delete();
  }

  public void testFetchSingleObject() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.getInBackground(currentObjectId).subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject o) {
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
    LCQuery query = new LCQuery("Student");
    query.limit(4);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> o) {
        for(LCObject obj: o) {
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
    LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
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
    LCQuery query = new LCQuery("Student");
    int cnt = query.count();
    assertTrue(cnt > 0);
    LCObject firstObj = query.getFirst();
    assertTrue(null != firstObj);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCObject> o) {
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
    LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    LCQuery q1 = query.clone();
    assertEquals(q1.getCachePolicy(), query.getCachePolicy());
    assertEquals(q1.getClassName(), query.getClassName());
    assertEquals(q1.getClazz(), query.getClazz());
    assertEquals(q1.getClass(), query.getClass());
  }

  public void testFirstQuery() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject o) {
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

  public void testGetInBackgroundWithIncludePointer() throws Exception {
    LCObject current = LCObject.createWithoutData("Student", currentObjectId);
    final LCObject target = new LCObject("Student");
    target.put("friend", current);
    target.save();
    LCQuery query = new LCQuery("Student");
    query.include("friend");
    query.getInBackground(target.getObjectId()).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject o) {
        LCObject friend = o.getLCObject("friend");
        testSucceed = "Automatic Tester".equals(friend.get("name"));
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    target.delete();
    assertTrue(testSucceed);
  }

  public void testFirstQueryWithIncludePointer() throws Exception {
    LCObject current = LCObject.createWithoutData("Student", currentObjectId);
    final LCObject target = new LCObject("Student");
    target.put("friend", current);
    target.save();
    LCQuery query = new LCQuery("Student");
    query.include("friend");
    query.whereExists("friend");
    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCObject o) {
        LCObject friend = o.getLCObject("friend");
        testSucceed = "Automatic Tester".equals(friend.get("name"));
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    target.delete();
    assertTrue(testSucceed);
  }

  public void testFirstQueryWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.whereGreaterThan("age", 119);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject o) {
        System.out.println("onNext "  + o.toString());
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("completed.");
        testSucceed = true;
        latch.countDown();
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testFirstQueryUnderCallbackWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.whereLessThan("age", -119);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.getFirstInBackground().subscribe(ObserverBuilder.buildSingleObserver(new GetCallback() {
      @Override
      public void done(LCObject object, LCException e) {
        testSucceed = (object == null) && (e == null);
        latch.countDown();
      }
    }));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testGetQueryWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.getInBackground("thisisnotexistedObject").subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject o) {
        System.out.println("onNext "  + o.toString());
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        testSucceed = true;
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

  public void testGetQueryUnderCallbackWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.getInBackground("thisisnotexistedObject").subscribe(ObserverBuilder.buildSingleObserver(new GetCallback() {
      @Override
      public void done(LCObject object, LCException e) {
        testSucceed = null != e;
        latch.countDown();
      }
    }));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQueryWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.whereGreaterThan("age", 119);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> list) {
        System.out.println("onNext result size: " + list.size());
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

  public void testQueryUnderCallbackWithEmptyResult() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.whereGreaterThan("age", 119);
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(new FindCallback<LCObject>() {
      @Override
      public void done(List<LCObject> LCObjects, LCException LCException) {
        System.out.println("onNext result size: " + LCObjects.size());
        testSucceed = LCException == null;
        latch.countDown();
      }
    }));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQueryCacheNotExisted() throws Exception {
    LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.limit(5);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ONLY);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> o) {
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
    LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.limit(5);
    query.skip(1);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ELSE_NETWORK);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> o) {
        for (LCObject j: o) {
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

  public void testQueryAllAfterClearCache() throws Exception {
    final LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.limit(5);
    query.skip(1);
    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(List<LCObject> o) {
        System.out.println("succeed to query at first time, result size: " + o.size());
        query.clearCachedResult();
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(List<LCObject> o) {
            System.out.println("succeed to query at second time, result size: " + o.size());
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to query at second time");
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("failed to query at first time");
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);

  }

  public void testQueryFirstAfterClearCache() throws Exception {
    final LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.limit(5);
    query.skip(1);
    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject o) {
        System.out.println("succeed to query at first time, result objectId: " + o.getObjectId());
        query.clearCachedResult();
        query.setCachePolicy(LCQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject o) {
            System.out.println("succeed to query at second time, result objectId: " + o.getObjectId());
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to query at second time");
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("failed to query at first time");
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);

  }

  public void testQueryCountAfterClearCache() throws Exception {
    final LCQuery query = new LCQuery("Student");
    query.orderByDescending(LCObject.KEY_CREATED_AT);
    query.limit(5);
    query.skip(1);
    query.countInBackground().subscribe(new Observer<Integer>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Integer o) {
        System.out.println("succeed to query at first time, count: " + o);
        query.clearCachedResult();
        query.setCachePolicy(LCQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.countInBackground().subscribe(new Observer<Integer>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(Integer o) {
            System.out.println("succeed to query at second time, count: " + o);
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to query at second time");
            throwable.printStackTrace();
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("failed to query at first time");
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
    LCQuery<LCObject> queryLikeRed = new LCQuery<>("FileUnitTest");
    queryLikeRed.include("taskId");
    queryLikeRed.include("taskId.userId");
    queryLikeRed.selectKeys(Arrays.asList("taskId.userId.nickName", "taskId.userId.username", "createdAt"));
    queryLikeRed.whereEqualTo("userId", LCObject.createWithoutData("_User", "userobjectid000000100008"));
    int total = queryLikeRed.count();
    List<LCObject> queryList = queryLikeRed.find();
    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> redList = new ArrayList<>();
    for (LCObject redPacket: queryList) {
      Map<String, Object> save = new HashMap<>();
      LCObject task = redPacket.getLCObject("taskId");
      System.out.println(task);
      LCUser user = task.getLCObject("userId");
      System.out.println(user);
    }

  }
}
