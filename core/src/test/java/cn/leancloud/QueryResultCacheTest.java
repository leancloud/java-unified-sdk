package cn.leancloud;

import cn.leancloud.callback.FindCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.json.JSON;
import cn.leancloud.query.LCQueryResult;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class QueryResultCacheTest extends TestCase {
  private static String className = QueryResultCacheTest.class.getSimpleName();
  private List<LCObject> resultObjects = new ArrayList<>(10);
  private boolean testSucceed = false;

  public QueryResultCacheTest(String name) {
    super(name);
    Configure.initializeRuntime();
    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
  }

  public static Test suite() {
    return new TestSuite(QueryResultCacheTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    LCQuery.clearAllCachedResults();
    testSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    for (LCObject o : resultObjects) {
      o.delete();
    }
    resultObjects.clear();
  }

  public void testDateCache() throws Exception {
    LCQuery<LCObject> query = new LCQuery<LCObject>("LegInfo_170");
    final CountDownLatch latch = new CountDownLatch(1);
    query.setLimit(1);
    query.order("legEndDate");
    query.setCachePolicy(LCQuery.CachePolicy.NETWORK_ELSE_CACHE);
    FindCallback<LCObject> cb = new FindCallback<LCObject>() {
      public void done(List<LCObject> avObjects, LCException e) {
        if (null != e) {
          System.out.println("first round query exception: " + e);
          latch.countDown();
          return;
        }
        System.out.println("first round query from network...");
        for (LCObject obj: avObjects) {
          System.out.println(obj.getDate("legEndDate"));
        }
        LCQuery<LCObject> query2 = new LCQuery<LCObject>("LegInfo_170");
        query2.setLimit(1);
        query2.order("legEndDate");
        query2.setCachePolicy(LCQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query2.findInBackground().subscribe(new Observer<List<LCObject>>() {
          @Override
          public void onSubscribe(@NotNull Disposable disposable) {

          }

          @Override
          public void onNext(@NotNull List<LCObject> lcObjects) {
            System.out.println("second round query from cache...");
            for (LCObject obj: lcObjects) {
              System.out.println(obj.getDate("legEndDate"));
            }
            testSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(@NotNull Throwable throwable) {
            System.out.println("second round query exception: " + throwable);
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildCollectionObserver(cb));
    latch.await();
    assertTrue(testSucceed);
  }

  public void testQueryResultSerializer() throws Exception {
    String input = "{\"results\":" +
            "[{\"updatedAt\":\"2019-05-18T13:11:58.194Z\",\"legStartDate\":{\"__type\":\"Date\",\"iso\":\"2019-05-18T13:06:02.584Z\"},\"legStats\":[[11.133333333333333,501,45,12.11111111111111,109,9,0.16666666666666666,1,6,5,45,501,1,0,2,0,0,0]],\"playerIds\":[\"5cdf07ce43e78c0068a1012f\"],\"displayNames\":[\"Steve97\"],\"objectId\":\"5ce0049eeaa37500671e4a59\",\"gameName\":\"x01\",\"initialScore\":501,\"settings\":[\"straight in\",\"double out\",\"20 rounds\",\"inputEachRound\",\"split bull\"],\"winAtRound\":15,\"createdAt\":\"2019-05-18T13:11:58.194Z\",\"deleteLegPlayerIds\":[],\"isActive\":true,\"legEndDate\":{\"__type\":\"Date\",\"iso\":\"2019-05-18T13:11:57.829Z\"},\"timeUsedForThisLeg\":355,\"playerNumbers\":1,\"vsType\":\"local\",\"totalScoreForEachRound\":[[62,25,22,61,26,37,40,28,21,57,34,40,24,19,5]]},{\"setUUID\":\"AA05EA4A-7DEC-438D-BBF9-DA55CAEA328F\",\"isValidSetLeg\":true,\"updatedAt\":\"2019-04-27T14:38:33.250Z\",\"legStartDate\":{\"__type\":\"Date\",\"iso\":\"2018-09-29T01:42:13.542Z\"},\"scoresForEachRound\":[[[60,60,60],[60,11,50]],[[60,60,60],[0,0,0]]],\"legStats\":[[50.166666666666664,301,6,50.166666666666664,301,6,1,1,1,121,0,301,2,1,0,1,0,1],[60,180,3,60,180,3,0,0,0,0,0,301,2,0,0,0,0,1]],\"playerIds\":[\"5a5d7d02d50eee00710e3e75\",\"5a655d889f5454298c23d103\"],\"displayNames\":[\"stacit\",\"breggs\"],\"objectId\":\"5cc469697b968a0073a48d49\",\"shootResultsForEachRound\":[[[[3,20],[3,20],[3,20]],[[3,20],[1,11],[2,25]]],[[[3,20],[3,20],[3,20]],[[0,0],[0,0],[0,0]]]],\"gameName\":\"x01\",\"initialScore\":301,\"settings\":[\"straight in\",\"double out\",\"20 rounds\",\"inputEachDart\",\"split bull\"],\"winAtRound\":2,\"createdAt\":\"2019-04-27T14:38:33.250Z\",\"winnerIds\":[\"5a5d7d02d50eee00710e3e75\"],\"legFlagInSet\":1,\"deleteLegPlayerIds\":[],\"isActive\":true,\"legEndDate\":{\"__type\":\"Date\",\"iso\":\"2018-09-29T01:42:34.417Z\"},\"timeUsedForThisLeg\":20,\"playerNumbers\":2,\"vsType\":\"bluetooth\",\"firstToX\":3},{\"updatedAt\":\"2019-04-27T14:38:42.056Z\",\"legStartDate\":{\"__type\":\"Date\",\"iso\":\"2018-09-16T16:43:34.371Z\"},\"scoresForEachRound\":[[[60,60,60],[60,60,60],[60,57,24]],[[60,60,20],[60,60,60],[0,0,0]]],\"legStats\":[[55.666666666666664,501,9,55.666666666666664,501,9,1,1,1,141,9,501,2,1,0,0,1,2],[53.333333333333336,320,6,53.333333333333336,320,6,0,0,0,0,0,501,2,0,0,0,1,1]],\"playerIds\":[\"5b9e74e6570c350063731c95\",\"5a8ee3ad128fe10037d57b5d\"],\"displayNames\":[\"dennis.cortez\",\"DARTERONG-PINOY U.K.\"],\"objectId\":\"5cc46972a91c93006cf7d3ca\",\"shootResultsForEachRound\":[[[[3,20],[3,20],[3,20]],[[3,20],[3,20],[3,20]],[[3,20],[3,19],[2,12]]],[[[3,20],[3,20],[1,20]],[[3,20],[3,20],[3,20]],[[0,0],[0,0],[0,0]]]],\"gameName\":\"x01\",\"initialScore\":501,\"settings\":[\"straight in\",\"double out\",\"20 rounds\",\"inputEachDart\",\"split bull\"],\"winAtRound\":3,\"createdAt\":\"2019-04-27T14:38:42.056Z\",\"winnerIds\":[\"5b9e74e6570c350063731c95\"],\"deleteLegPlayerIds\":[],\"isActive\":true,\"legEndDate\":{\"__type\":\"Date\",\"iso\":\"2018-09-16T16:44:19.731Z\"},\"timeUsedForThisLeg\":45,\"playerNumbers\":2,\"vsType\":\"bluetooth\"}]}";
    LCQueryResult qr = JSON.parseObject(input, LCQueryResult.class);
    qr.setClassName(className);
    for (LCObject obj : qr.getResults()) {
      obj.setClassName(className);
      System.out.println("legStartDate(first) is: " + obj.getDate("legStartDate"));
    }
    String jsonStr = qr.toJSONString();
    System.out.println(jsonStr);
    LCQueryResult qr2 = JSON.parseObject(jsonStr, LCQueryResult.class);
    for (LCObject obj : qr2.getResults()) {
      System.out.println("legStartDate(second) is: " + obj.getDate("legStartDate"));
    }
  }
}
