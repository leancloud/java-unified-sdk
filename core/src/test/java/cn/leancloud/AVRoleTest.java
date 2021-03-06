package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVRoleTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public AVRoleTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVRoleTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testModifyCurrentRole() throws Exception {
    AVQuery<AVRole> query = AVRole.getQuery();
    query.whereEqualTo("name", "Admin");
    AVRole target = query.getFirst();
    target.getUsers().add(AVObject.createWithoutData(AVUser.class, "5dd7892143c2570074c96ca9"));
    target.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull AVObject avObject) {
        System.out.println(avObject.toJSONString());
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(@NotNull Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testCreateRole() throws Exception {
    AVQuery query = AVRole.getQuery();
    query.whereEqualTo("name", "Admin");
    query.findInBackground().subscribe(new Observer<List<AVRole>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVRole> o) {
        if (null == o || o.size() < 1) {
          AVACL  acl = new AVACL();
          acl.setPublicReadAccess(true);
          acl.setPublicWriteAccess(false);
          AVRole role = new AVRole("Admin", acl);
          role.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {

            }

            public void onNext(AVObject avObject) {
              testSucceed = true;
              latch.countDown();
            }

            public void onError(Throwable throwable) {
              latch.countDown();
            }

            public void onComplete() {

            }
          });
        } else {
          AVACL  acl = new AVACL();
          acl.setPublicReadAccess(true);
          acl.setPublicWriteAccess(false);
          AVRole role = new AVRole("Admin", acl);
          role.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {

            }

            public void onNext(AVObject avObject) {
              latch.countDown();
            }

            public void onError(Throwable throwable) {
              testSucceed = true;
              latch.countDown();
            }

            public void onComplete() {

            }
          });
        }
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
    assertTrue(testSucceed);
  }
}
