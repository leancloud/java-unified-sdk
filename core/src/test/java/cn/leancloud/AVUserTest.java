package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AVUserTest extends TestCase {
  public AVUserTest(String name) {
    super(name);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite() {
    return new TestSuite(AVUserTest.class);
  }

  @Override
  protected void setUp() throws Exception {
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

  public void testCurrentUser() throws Exception {
    AVUser.disableAutomaticUser();
    AVUser currentUser = AVUser.getCurrentUser();
    assertNull(currentUser);
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
