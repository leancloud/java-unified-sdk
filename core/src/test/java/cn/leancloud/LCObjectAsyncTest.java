package cn.leancloud;


import cn.leancloud.core.AppConfiguration;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class LCObjectAsyncTest extends TestCase {
  private boolean testSucceed = false;
  private CountDownLatch latch = null;

  public LCObjectAsyncTest(String testName) {
    super(testName);
    AppConfiguration.config(true, new AppConfiguration.SchedulerCreator() {
      public Scheduler create() {
        return Schedulers.newThread();
      }
    });
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectAsyncTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testCreateObject() throws Exception {
    LCObject object = new LCObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println(LCObject.toString());
        System.out.println("try to remove grade field.");
        LCObject.remove("grade");
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(LCObject LCObject) {
            System.out.println(LCObject.toString());
            System.out.println("remove field finished.");
            LCObject.deleteInBackground().subscribe(new Observer<LCNull>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(LCNull object) {
                System.out.println("delete finished.");
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
          }

          public void onError(Throwable throwable) {
            throwable.printStackTrace();
            latch.countDown();
          }

          public void onComplete() {
          }
        });

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

  public void testObjectCreateEx() throws Exception{
    LCObject post = new LCObject("Post");
    post.put("title", "LeanCloud 发布了新版 Java 统一 SDK");
    post.put("content", "9 月初，LeanCloud 发布了新版 Java 统一 SDK，欢迎大家试用。。。");
    post.saveInBackground().map(new Function<LCObject, LCObject>() {
      public LCObject apply(LCObject p) throws Exception {
        // 在 Post 保存成功之后，再新建一个 Comment 对象.
        LCObject comment = new LCObject("Comment");
        comment.put("content", "好想试一下");
        comment.put("post", p);
        return comment.saveInBackground().blockingFirst();
      };
    }).subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject comment) {
        System.out.print("succeed to save post and comment objects.");
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.print("failed to save post or comment objects. cause: " + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testObjectRefresh() throws Exception {
    LCObject object = new LCObject("Student");
    object.setObjectId("5a7a4ac8128fe1003768d2b1");
    object.refreshInBackground().subscribe(new Observer<LCObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCObject LCObject) {
        System.out.println("subscribe result: " + LCObject.toString());
        testSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("subscribe completed.");
      }
    });
    latch.await();
    assertTrue(testSucceed);
  }
}
