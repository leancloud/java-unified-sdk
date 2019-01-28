package cn.leancloud;

import cn.leancloud.callback.FollowersAndFolloweesCallback;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AVUserFollowshipTest extends TestCase {
  private boolean operationSucceed = false;
  public AVUserFollowshipTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVUserFollowshipTest.class);
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

  public void testFolloweeQuery() throws Exception {
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

        AVQuery<AVUser> query = avUser.followeeQuery(AVUser.class);
        List<AVUser> followees = query.find();
        if (null == followees || followees.size() < 1) {
          avUser.followInBackground("5bff452afb4ffe0069a9893e").subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(JSONObject jsonObject) {
              System.out.println(jsonObject.toJSONString());
              operationSucceed = true;
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

        } else {
          operationSucceed = true;
          latch.countDown();
        }
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFollowerQuery() throws Exception {
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

        AVQuery<AVUser> query = avUser.followerQuery(AVUser.class);
        List<AVUser> followers = query.find();
        operationSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testBeFollowed() throws Exception {
    AVUser user = new AVUser();
    user.setEmail("jfeng001@test.com");
    user.setUsername("jfeng001");
    user.setPassword("FER$@$@#Ffwe");
    user.signUp();
    AVUser logginUser = AVUser.logIn("jfeng001", "FER$@$@#Ffwe").blockingFirst();
    logginUser.followInBackground("5bff479067f3560066d00676").blockingFirst();
  }

  public void testFollowUserNotLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery<? extends AVUser> query = AVUser.getQuery();
    query.findInBackground().subscribe(new Observer<List<? extends AVUser>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<? extends AVUser> avUsers) {
        AVUser target = avUsers.get(0);
        target.followInBackground("5bff479067f3560066d00676").subscribe(new Observer<JSONObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(JSONObject jsonObject) {
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            throwable.printStackTrace();
            operationSucceed = true;
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
    assertTrue(operationSucceed);
  }

  public void testFolloweeAndFollowerQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logIn("jfeng", "FER$@$@#Ffwe").subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        avUser.getFollowersAndFolloweesInBackground(new FollowersAndFolloweesCallback() {
          @Override
          public void done(Map avObjects, AVException avException) {
            operationSucceed = (null != avObjects);
            System.out.println(JSON.toJSONString(avObjects));
            latch.countDown();
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
