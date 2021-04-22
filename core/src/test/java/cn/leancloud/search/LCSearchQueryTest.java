package cn.leancloud.search;

import cn.leancloud.LCObject;
import cn.leancloud.Configure;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCSearchQueryTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;
  public LCSearchQueryTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCSearchQueryTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    testSucceed = false;
    latch = new CountDownLatch(1);
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testGenericQuery() throws Exception {
    List<String> fields = new ArrayList<>();
    fields.add("content");
    fields.add("author");
    LCSearchQuery searchQuery = new LCSearchQuery("文件");
    searchQuery.setClassName("Ticket");
    searchQuery.setSkip(0);
    searchQuery.setLimit(3);
    searchQuery.setFields(fields);
    searchQuery.findInBackground().subscribe(new Observer<List<LCObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCObject> results) {
        for (LCObject o:results) {
          System.out.println(o);
        }
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        testSucceed = true;
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
