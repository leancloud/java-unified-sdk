package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.CountDownLatch;

public class AVUserTest extends TestCase {
  private boolean operationSucceed = false;
  public AVUserTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVUserTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public void testSingupWithEmail() throws Exception {
    AVUser user = new AVUser();
    user.setEmail("jfeng@test.com");
    user.setUsername("jfeng");
    user.setPassword("FER$@$@#Ffwe");
    user.signUpInBackground().subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVUser avUser) {

      }

      public void onError(Throwable throwable) {
        assertNotNull(throwable);
      }

      public void onComplete() {

      }
    });
  }

  public void testLogin() throws Exception {
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    Thread.sleep(3000);
  }

  public void testAnonymousLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logInAnonymously().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
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

  public void testDisassociateAnonymousLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logInAnonymously().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        System.out.println("logInAnonymously onNext. result=" + avUser.toString());
        avUser.dissociateWithAuthData("anonymous").subscribe(new Observer<AVUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVUser avUser) {
            System.out.println("dissociateWithAuthData onNext. result=" + avUser.toString());
            operationSucceed = true;
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("failed to dissocaite auth data. cause: " + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
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

  public void testCurrentUser() throws Exception {
    AVUser.disableAutomaticUser();
    AVUser currentUser = AVUser.getCurrentUser();
    assertNotNull(currentUser);
  }

  public void testCurrentUserWithNew() throws Exception {
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        AVUser.changeCurrentUser(avUser, true);
        AVUser u = AVUser.getCurrentUser();
        assertTrue(avUser.equals(u));
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
  }

  public void testCurrentUserWithCached() throws Exception {
    ;
  }

  public void testCurrentUserWithSubclass() throws Exception {
    ;
  }

  public void testCheckAuthenticatedFalse() throws Exception {
    AVUser u = new AVUser();
    u.setEmail("jfeng@test.com");
    u.setUsername("jfeng");
    u.setPassword("FER$@$@#Ffwe");
    u.setObjectId("ferewr2343");
    u.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(Boolean aBoolean) {
        if (aBoolean) {
          fail();
        }
      }

      public void onError(Throwable throwable) {

      }

      public void onComplete() {

      }
    });
  }
  public void testCheckAuthenticatedTrue() throws Exception {
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        avUser.checkAuthenticatedInBackground().subscribe(new Observer<Boolean>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(Boolean aBoolean) {
            if (!aBoolean) {
              fail();
            }
          }

          public void onError(Throwable throwable) {
            fail();
          }

          public void onComplete() {

          }
        });
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {
      }
    });
  }
}
