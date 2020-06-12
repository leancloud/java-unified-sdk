package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AVRelationTest extends TestCase {
  private boolean testSucceed = false;
  public AVRelationTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVRelationTest.class);
  }

  public void testRoleRelationQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    AVQuery<AVRole> roleQuery=new AVQuery<AVRole>("_Role");
    roleQuery.whereEqualTo("name", "CTO");
    roleQuery.findInBackground().subscribe(new Observer<List<AVRole>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVRole> avRoles) {
        System.out.println("get target roles.");
        if (avRoles.size() < 1) {
          latch.countDown();
          return;
        }
        AVRole administrator = avRoles.get(0);
        AVRelation userRelation = administrator.getUsers();
        AVQuery<AVUser> query = userRelation.getQuery(AVUser.class);
        query.findInBackground().subscribe(new Observer<List<AVUser>>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(List<AVUser> avUsers) {
            System.out.println("get relation users.");
            testSucceed = true;
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
