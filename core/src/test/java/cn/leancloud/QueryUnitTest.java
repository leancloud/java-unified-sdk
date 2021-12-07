package cn.leancloud;

import cn.leancloud.callback.CountCallback;
import cn.leancloud.callback.DeleteCallback;
import cn.leancloud.callback.FindCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.query.LCQueryResult;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryUnitTest extends TestCase {
  private static String className = QueryUnitTest.class.getSimpleName();
  private List<LCObject> resultObjects = new ArrayList<>(10);
  private boolean testSucceed = false;

  public QueryUnitTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(QueryUnitTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    LCQuery.clearAllCachedResults();
    setUpClass();
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    for (LCObject o : resultObjects) {
      o.delete();
    }
    resultObjects.clear();
  }

  public void setUpClass() {
    resultObjects.clear();
    try {
      for (int i = 0; i < 5; i++) {
        LCObject player = new LCObject("QueryUnitTestPlayer");
        player.put("name", "player" + i);
        player.put("age", 30 + i);
        if (i % 2 == 0) {
          player.put("image", i);
        }
        LCObject obj = new LCObject(className);
        obj.put("playerName", "player" + i);
        obj.put("player", player);
        obj.put("score", i * 100);
        obj.addAll("scores", Arrays.asList(i, i + 1, i + 2, i + 3));
        obj.save();
        resultObjects.add(player);
        resultObjects.add(obj);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }


  public void testBasicQuery() throws Exception {
    LCQuery<LCObject> query = new LCQuery<LCObject>(className);
    query.whereEqualTo("playerName", "player1");
    final CountDownLatch latch = new CountDownLatch(1);
    FindCallback<LCObject> cb = new FindCallback<LCObject>() {
      public void done(List<LCObject> avObjects, LCException e) {
        if (null != e) {
          latch.countDown();
          return;
        }
        for (LCObject obj : avObjects) {
          if (!"player1".equals(obj.get("playerName"))) {
            testSucceed = false;
            break;
          } else {
            testSucceed = true;
          }
        }
        latch.countDown();
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);
    List<LCObject> LCObjects = query.find();
    for (LCObject obj : LCObjects) {
      assertEquals("player1", obj.get("playerName"));
    }
    assertTrue(LCObjects != null && LCObjects.size() > 0);
  }

  public void testQueryWithConditions() throws Exception {
    // whereNotEqualTo
    LCQuery<LCObject> query = new LCQuery<LCObject>(className);
    query.whereNotEqualTo("playerName", "player1");

    final CountDownLatch latch = new CountDownLatch(1);

    FindCallback<LCObject> cb = new FindCallback<LCObject>() {
      public void done(List<LCObject> avObjects, LCException e) {
        if (null != e) {
          latch.countDown();
        }
        testSucceed = true;
        latch.countDown();
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);

    // whereGreaterThan
    query = new LCQuery<LCObject>(className);
    query.whereGreaterThan("score", 200);
    List<LCObject> LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertTrue(obj.getInt("score") > 200);
    }

    // whereLessThan
    query = new LCQuery<LCObject>(className);
    query.whereLessThan("score", 200);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertTrue(obj.getInt("score") < 200);
    }

    // whereLessThanOrEqualTo
    query = new LCQuery<LCObject>(className);
    query.whereLessThanOrEqualTo("score", 200);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertTrue(obj.getInt("score") <= 200);
    }

    // whereGreaterThanOrEqualTo
    query = new LCQuery<LCObject>(className);
    query.whereGreaterThanOrEqualTo("score", 200);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertTrue(obj.getInt("score") >= 200);
    }

    // whereNotContainedIn
    query = new LCQuery<LCObject>(className);
    List<String> names = Arrays.asList("player1", "player0");
    query.whereNotContainedIn("playerName", names);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertNotContains(obj.get("playerName"), names);
    }

    // whereContainedIn
    query = new LCQuery<LCObject>(className);
    query.whereContainedIn("playerName", names);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertContains(obj.get("playerName"), names);
    }

    // whereMatches
    query = new LCQuery<LCObject>(className);
    query.whereMatches("playerName", "^[a-zA-Z]+1");
    LCObjects = query.find();
    for (LCObject obj : LCObjects) {
      assertEquals("player1", obj.get("playerName"));
    }
    assertTrue(LCObjects != null && LCObjects.size() > 0);

    // whereContains
    query = new LCQuery<LCObject>(className);
    query.whereContains("playerName", "0");
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertEquals("player0", obj.get("playerName"));
    }

    // whereEndsWith
    query = new LCQuery<LCObject>(className);
    query.whereEndsWith("playerName", "0");
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertEquals("player0", obj.get("playerName"));
    }

  }

  public void testQueryArrayKey() throws Exception {
    LCQuery query = new LCQuery(className);
    query.whereEqualTo("scores", 2);
    List<LCObject> LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      List scores = obj.getList("scores");
      assertContains(2, scores);
    }

    // whereContainsAll
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    numbers.add(2);
    numbers.add(3);
    numbers.add(4);
    numbers.add(5);
    query = new LCQuery(className);
    query.whereContainsAll("scores", numbers);
    LCObjects = query.find();
    assertFalse(LCObjects.isEmpty());
    for (LCObject obj : LCObjects) {
      assertTrue(numbers.equals(obj.getList("scores")));
    }

  }

  public void testCountObjects() throws Exception {
    LCQuery query = new LCQuery(className);
    query.whereEqualTo("scores", 2);
    final CountDownLatch latch = new CountDownLatch(1);
    CountCallback cb = new CountCallback() {

      @Override
      public void done(int count, LCException e) {
        testSucceed = null == e;
        latch.countDown();
      }
    };
    query.countInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);
    assertTrue(query.count() > 0);
  }

  public void testQueryWithPointer() throws Exception {
    LCQuery<LCObject> query = new LCQuery<LCObject>("QueryUnitTestPlayer");
    final LCObject player = query.getFirst();

    query = LCQuery.getQuery(className);
    query.whereEqualTo("player", player);

    final CountDownLatch latch = new CountDownLatch(1);
    FindCallback<LCObject> cb = new FindCallback<LCObject>() {

      @Override
      public void done(List<LCObject> avObjects, LCException avException) {
        if (null == avObjects || avObjects.size() < 1) {
          latch.countDown();
          return;
        }
        testSucceed = true;
        latch.countDown();
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testWhereMatchesQuery() throws Exception {
    LCQuery<LCObject> innerQuery = LCQuery.getQuery("QueryUnitTestPlayer");
    innerQuery.whereExists("image");
    LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.whereMatchesQuery("player", innerQuery);
    query.include("player");

    final CountDownLatch latch = new CountDownLatch(1);

    FindCallback<LCObject> cb = new FindCallback<LCObject>() {
      public void done(List<LCObject> avObjects, LCException e) {
        if (null != e || avObjects.size() < 1) {
          latch.countDown();
          return;
        }
        for (LCObject obj : avObjects) {
          if (obj.getLCObject("player").has("image")) {
            testSucceed = true;
          } else {
            testSucceed = false;
            break;
          }
        }
        latch.countDown();
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testWhereDoesNotMatchQuery() throws Exception {
    LCQuery<LCObject> innerQuery = LCQuery.getQuery("QueryUnitTestPlayer");
    innerQuery.whereExists("image");
    LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.whereDoesNotMatchQuery("player", innerQuery);
    query.include("player");
    List<LCObject> biggerResult = query.find();
    Set<String> playerIds = new HashSet<String>();
    for(LCObject o:biggerResult){
      if (null != o.getLCObject("player")) {
        playerIds.add(o.getLCObject("player").getObjectId());
      }
    }
    List<LCObject> innerResult = innerQuery.find();
    boolean flag = false;
    for(LCObject o:innerResult){
      if(playerIds.contains(o.getObjectId())){
        flag = true;
        break;
      }
    }
    Assert.assertFalse(flag);
  }

  public void testQueryCacheOnly() throws Exception {
    final LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ONLY);

    List<LCObject> objects = query.find();
    assertTrue(objects.size() < 1);
  }

  public void testQueryNetworkOnly() throws Exception {
    LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.NETWORK_ONLY);
    List<LCObject> objects = query.find();
    assertTrue(objects.size() > 0);
    assertTrue(query.hasCachedResult());

    query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ONLY);
    List<LCObject> objectsInCache = query.find();
    assertFalse(objectsInCache.isEmpty());
//    assertEquals(objectsInCache, objects);

    // clear cache
    LCQuery.clearAllCachedResults();
    final LCQuery<LCObject> missQuery = LCQuery.getQuery(className);
    missQuery.setCachePolicy(LCQuery.CachePolicy.CACHE_ONLY);
    List<LCObject> result = missQuery.find();
    assertTrue(result.size() < 1);
  }

  public void testQueryCacheElseNetwork() throws Exception {
    LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ELSE_NETWORK);
    List<LCObject> objects = query.find();
    assertTrue(objects.size() > 0);

    // Next time,we can get it from cache
    query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_ONLY);
    List<LCObject> objectsInCache = query.find();
    assertFalse(objectsInCache.isEmpty());
//    assertEquals(objectsInCache, objects);
  }

  public void testQueryCacheDeserializer() throws Exception {
    String content = "{\"className\":\"QueryUnitTest\",\"count\":0,\"results\":[{\"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":0,\"createdAt\":\"2019-01-03T05:03:26.361Z\",\"playerName\":\"player0\",\"scores\":[0,1,2,3],\"objectId\":\"5c2d979e1579a3005f9296e5\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d971b4773f717360327d2\"}},\"updatedAt\":\"2019-01-03T05:03:26.364Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":100,\"createdAt\":\"2019-01-03T05:03:30.704Z\",\"playerName\":\"player1\",\"scores\":[1,2,3,4],\"objectId\":\"5c2d97a24773f71736032f49\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a14773f71736032f3e\"}},\"updatedAt\":\"2019-01-03T05:03:30.782Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":200,\"createdAt\":\"2019-01-03T05:03:34.395Z\",\"playerName\":\"player2\",\"scores\":[2,3,4,5],\"objectId\":\"5c2d97a667f356005fdd9a8f\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a47f6fd3005dc5ae6c\"}},\"updatedAt\":\"2019-01-03T05:03:34.398Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":300,\"createdAt\":\"2019-01-03T05:03:37.446Z\",\"playerName\":\"player3\",\"scores\":[3,4,5,6],\"objectId\":\"5c2d97a967f356005fdd9ad9\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a7fb4ffe005fdde06a\"}},\"updatedAt\":\"2019-01-03T05:03:37.449Z\"}},{\"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":400,\"createdAt\":\"2019-01-03T05:03:40.987Z\",\"playerName\":\"player4\",\"scores\":[4,5,6,7],\"objectId\":\"5c2d97ac9f54540070306b8a\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97ab1579a3005f929777\"}},\"updatedAt\":\"2019-01-03T05:03:40.991Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":0,\"createdAt\":\"2019-01-07T02:46:53.538Z\",\"playerName\":\"player0\",\"scores\":[0,1,2,3],\"objectId\":\"5c32bd9d44d904005d310fcd\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f629b\"}},\"updatedAt\":\"2019-01-07T02:46:53.541Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":100,\"createdAt\":\"2019-01-07T02:46:53.627Z\",\"playerName\":\"player1\",\"scores\":[1,2,3,4],\"objectId\":\"5c32bd9d67f35600634f5401\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d0b61600067a811d8\"}},\"updatedAt\":\"2019-01-07T02:46:53.630Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":200,\"createdAt\":\"2019-01-07T02:46:53.768Z\",\"playerName\":\"player2\",\"scores\":[2,3,4,5],\"objectId\":\"5c32bd9d0b61600067a811dc\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d44d904005d310fcf\"}},\"updatedAt\":\"2019-01-07T02:46:53.772Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":300,\"createdAt\":\"2019-01-07T02:46:53.839Z\",\"playerName\":\"player3\",\"scores\":[3,4,5,6],\"objectId\":\"5c32bd9dfb4ffe1186b40677\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f62a2\"}},\"updatedAt\":\"2019-01-07T02:46:53.843Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":400,\"createdAt\":\"2019-01-07T02:46:53.919Z\",\"playerName\":\"player4\",\"scores\":[4,5,6,7],\"objectId\":\"5c32bd9d67f35600634f540e\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f62a5\"}},\"updatedAt\":\"2019-01-07T02:46:53.922Z\"}}]}";
    LCQueryResult result = LCQueryResult.fromJSONString(content);
    assertNotNull(result);
    assertTrue(result.getResults().size() > 0);
  }
  public void testQueryCacheThenNetwork() throws Exception {
    LCQuery<LCObject> query = LCQuery.getQuery(className);
    query.setCachePolicy(LCQuery.CachePolicy.CACHE_THEN_NETWORK);
    final AtomicInteger counter = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(1);
    FindCallback<LCObject> cb = new FindCallback<LCObject>() {

      @Override
      public void done(List<LCObject> avObjects, LCException avException) {

        if (avException != null) {
          testSucceed = (LCException.CACHE_MISS == avException.getCode());
          latch.countDown();
        }
        testSucceed = (avObjects != null && avObjects.size() > 0);
        if (counter.incrementAndGet() < 2) {
          testSucceed = false;
        }
        latch.countDown();
      }

    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
  }

  public void testCompondQuery() throws Exception {
    LCQuery<LCObject> lotsOfWins = LCQuery.getQuery(className);
    lotsOfWins.whereGreaterThan("score", 150);

    LCQuery<LCObject> fewWins = LCQuery.getQuery(className);
    fewWins.whereLessThan("score", 5);

    List<LCQuery<LCObject>> queries = new ArrayList<LCQuery<LCObject>>();
    queries.add(lotsOfWins);
    queries.add(fewWins);

    LCQuery<LCObject> mainQuery = LCQuery.or(queries);
    final CountDownLatch latch = new CountDownLatch(1);
    FindCallback<LCObject> cb = new FindCallback<LCObject>() {
      public void done(List<LCObject> results, LCException e) {
        testSucceed = null != results && results.size() > 0;
        latch.countDown();
      }
    };
    mainQuery.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testDeleteAll() throws Exception {
    for (int i = 0; i < 5; i++) {
      LCObject obj = new LCObject("QueryUnitTest_DeleteAll");
      obj.put("number", i);
      obj.save();
    }
    LCQuery<LCObject> query = new LCQuery<LCObject>("QueryUnitTest_DeleteAll");
    List<LCObject> list = query.find();
    assertTrue(list.size() > 0);
    query.deleteAll();
    list = query.find();
    assertTrue(list.size() == 0);
  }

  public void testWhereSizeEqual() throws Exception {
    LCQuery<LCObject> query = new LCQuery<LCObject>(className);
    query.whereSizeEqual("scores", 4);
    List<LCObject> objects = query.find();
    assertTrue(objects.size() > 0);
    for (LCObject obj : objects) {
      assertTrue(4 == obj.getList("scores").size());
    }
  }

  public void testDeleteAllInBackground() throws Exception {
    for (int i = 0; i < 5; i++) {
      LCObject obj = new LCObject("QueryUnitTest_DeleteAll");
      obj.put("number", i);
      obj.save();
    }
    LCQuery<LCObject> query = new LCQuery<LCObject>("QueryUnitTest_DeleteAll");
    List<LCObject> list = query.find();
    assertTrue(list.size() > 0);

    final CountDownLatch latch = new CountDownLatch(1);
    DeleteCallback cb = new DeleteCallback() {
      @Override
      public void done(LCException e) {
        testSucceed = null == e;
        latch.countDown();
      }
    };
    query.deleteAllInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    latch.await();
    assertTrue(testSucceed);

    list = query.find();
    assertTrue(list.size() == 0);
  }

  private void assertNotContains(Object x, Collection<?> list) {
    for (Object obj : list) {
      assertFalse(x.equals(obj));
    }
  }

  private void assertContains(Object x, Collection<?> list) {
    boolean contains = false;
    for (Object obj : list) {
      if (x.equals(obj)) {
        contains = true;
      }
    }
    assertTrue(contains);
  }
}
