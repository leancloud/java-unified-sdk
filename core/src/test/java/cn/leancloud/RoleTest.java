package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RoleTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  public RoleTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(RoleTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
  }

  public void testModifyCurrentRole() throws Exception {
    LCQuery<LCRole> query = LCRole.getQuery();
    query.whereEqualTo("name", "Admin");
    LCRole target = query.getFirst();
    target.getUsers().add(LCObject.createWithoutData(LCUser.class, "5dd7892143c2570074c96ca9"));
    target.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(@NotNull Disposable disposable) {

      }

      @Override
      public void onNext(@NotNull LCObject LCObject) {
        System.out.println(LCObject.toJSONString());
        testSucceed = true;
        latch.countDown();
      }

      @Override
      public void onError(@NotNull Throwable throwable) {
        if (throwable.getMessage().indexOf("Forbidden writing by object's ACL") >= 0) {
          testSucceed = true;
        } else {
          throwable.printStackTrace();
        }
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
    LCQuery query = LCRole.getQuery();
    query.whereEqualTo("name", "Admin");
    query.findInBackground().subscribe(new Observer<List<LCRole>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCRole> o) {
        if (null == o || o.size() < 1) {
          LCACL acl = new LCACL();
          acl.setPublicReadAccess(true);
          acl.setPublicWriteAccess(false);
          LCRole role = new LCRole("Admin", acl);
          role.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {

            }

            public void onNext(LCObject LCObject) {
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
          LCACL acl = new LCACL();
          acl.setPublicReadAccess(true);
          acl.setPublicWriteAccess(false);
          LCRole role = new LCRole("Admin", acl);
          role.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {

            }

            public void onNext(LCObject LCObject) {
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
