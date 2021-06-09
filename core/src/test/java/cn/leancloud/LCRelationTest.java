package cn.leancloud;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LCRelationTest extends TestCase {
  private boolean testSucceed = false;
  public LCRelationTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(LCRelationTest.class);
  }

  public void testRoleRelationQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    testSucceed = false;
    LCQuery<LCRole> roleQuery=new LCQuery<LCRole>("_Role");
    roleQuery.whereEqualTo("name", "CTO");
    roleQuery.findInBackground().subscribe(new Observer<List<LCRole>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<LCRole> avRoles) {
        System.out.println("get target roles.");
        if (avRoles.size() < 1) {
          latch.countDown();
          return;
        }
        LCRole administrator = avRoles.get(0);
        LCRelation userRelation = administrator.getUsers();
        LCQuery<LCUser> query = userRelation.getQuery(LCUser.class);
        query.findInBackground().subscribe(new Observer<List<LCUser>>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(List<LCUser> avUsers) {
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
