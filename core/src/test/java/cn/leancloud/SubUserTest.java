package cn.leancloud;

import cn.leancloud.auth.UserBasedTestCase;
import cn.leancloud.json.JSON;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SubUserTest extends UserBasedTestCase {
  private boolean operationSucceed = false;
  public SubUserTest(String name) {
    super(name);
    LCUser.alwaysUseSubUserClass(SubUser.class);
  }

  public static Test suite() {
    return new TestSuite(SubUserTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSingupWithEmail() throws Exception {
    SubUser user = new SubUser();
    user.setEmail("jfeng@test.com");
    user.setUsername("jfeng");
    user.setPassword("FER$@$@#Ffwe");
    final CountDownLatch latch = new CountDownLatch(1);
    user.signUpInBackground().subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(LCUser avUser) {
        System.out.println(JSON.toJSONString(avUser));
        operationSucceed = true;
        latch.countDown();

      }

      public void onError(Throwable throwable) {
        operationSucceed = true;
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser));
        operationSucceed = avUser instanceof SubUser;

        LCUser currentUser = LCUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        operationSucceed = operationSucceed & (currentUser instanceof SubUser);
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testAnonymousLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logInAnonymously().subscribe(new Observer<LCUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        operationSucceed = avUser instanceof SubUser;
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
    assertTrue(operationSucceed);
  }

  public void testQueryUser() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCQuery<? extends LCUser> query = LCUser.getQuery();
    query.findInBackground().subscribe(new Observer<List<? extends LCUser>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<? extends LCUser> avUsers) {
        operationSucceed = true;
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
    assertTrue(operationSucceed);
  }

  public void testCurrentUserWithNew() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        LCUser.changeCurrentUser(avUser, true);
        LCUser u = LCUser.getCurrentUser();
        operationSucceed = avUser.equals(u);
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testCurrentUserWithCached() throws Exception {
    LCUser.changeCurrentUser(null, true);
    LCUser current = LCUser.getCurrentUser();
    assertNull(current);
  }

  public void testCheckAuthenticatedFalse() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    SubUser u = new SubUser();
    u.setEmail("jfeng@test.com");
    u.setUsername("jfeng");
    u.setPassword("FER$@$@#Ffwe");
    u.setObjectId("ferewr2343");
    u.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Boolean aBoolean) {
        operationSucceed = aBoolean;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(!operationSucceed);
  }

  public void testCheckAuthenticatedTrue() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    LCUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<LCUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(LCUser avUser) {
        avUser.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(Boolean aBoolean) {
            operationSucceed = aBoolean;
            latch.countDown();
          }

          public void onError(Throwable throwable) {
            latch.countDown();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }
}
