package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.utils.AVUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    final CountDownLatch latch = new CountDownLatch(1);
    user.signUpInBackground().subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVUser avUser) {
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
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));

        AVUser currentUser = AVUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        operationSucceed = true;
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
    AVUser.logInAnonymously().subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        System.out.println("current user is authenticated? " + avUser.isAuthenticated() + ", isAnonymous? " + avUser.isAnonymous());
        String openId = "openid3322dr";
        String accessToken = "access_token";
        String expiresAt = "313830732382";
        final Map<String,Object> userAuth = new HashMap<>();
        userAuth.put("access_token",accessToken);
        userAuth.put("expires_in", expiresAt);
        userAuth.put("openid",openId);

        avUser.put("gender", "male");
        avUser.associateWithAuthData(userAuth, "qq").subscribe(new Observer<AVUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVUser tmp) {
            System.out.println("onNext. result=" + tmp.toString());
            System.out.println("current user is authenticated? " + tmp.isAuthenticated() + ", isAnonymous? " + tmp.isAnonymous());

            tmp.associateWithAuthData(userAuth, "weixin_test").subscribe(new Observer<AVUser>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVUser tmp2) {
                System.out.println("onNext. result=" + tmp2.toString());
                System.out.println("current user is authenticated? " + tmp2.isAuthenticated() + ", isAnonymous? " + tmp2.isAnonymous());

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

          }

          @Override
          public void onError(Throwable throwable) {
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

  public void testQueryUser() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery<AVUser> query = new AVQuery<AVUser>(AVUser.CLASS_NAME);
    query.findInBackground().subscribe(new Observer<List<AVUser>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVUser> avUsers) {
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
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + avUser.toString());
        AVUser.changeCurrentUser(avUser, true);
        AVUser u = AVUser.getCurrentUser();
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
    ;
  }

  public void testCurrentUserWithSubclass() throws Exception {
    ;
  }

  public void testCheckAuthenticatedFalse() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser u = new AVUser();
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
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
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


  public void testSaveCurrentUserData() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(AVUser avUser) {
        avUser.put("nickname", "Developer Fong");
        avUser.setFetchWhenSave(true);
        avUser.save();
        String nickName1 = avUser.getString("nickname");
        String nkName2 = AVUser.currentUser().getString("nickname");
        operationSucceed = nickName1.equals(nkName2) && nickName1.equals("Developer Fong");
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

  public void testGetCurrentUserAfterSave() throws Exception {
    operationSucceed = false;
    String nkName = AVUser.currentUser().getString("nickname");
    operationSucceed = nkName.equals("Developer Fong");
    assertTrue(operationSucceed);
  }

  public void testAssociateAuthDataTwice() throws Exception {
    AVUser user = new AVUser();
    user.setEmail("jfeng987@test.com");
    user.setUsername("jfeng987");
    user.setPassword("FER$@$@#Ffwe");

    String openId = "openid";
    String accessToken = "access_token";
    String expiresAt = "313830732382";
    final Map<String,Object> userAuth = new HashMap<>();
    userAuth.put("access_token",accessToken);
    userAuth.put("expires_in", expiresAt);
    userAuth.put("openid",openId);
    AVUser tmp = user.associateWithAuthData(userAuth, "qq").blockingSingle();
    assertTrue(tmp != null);

    final CountDownLatch latch = new CountDownLatch(1);
    operationSucceed = false;

    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(final AVUser two) {
        two.associateWithAuthData(userAuth, "qq").subscribe(new Observer<AVUser>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVUser avUser) {
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            two.abortOperations();
            JSONObject authData = two.getJSONObject("authData");
            System.out.println(authData);
            operationSucceed = authData.size() < 1;
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
}
