package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AVObjectACLTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  private static Map<String, Object> authData = new HashMap<>();
  static {
    authData.put("access_token", "weixin access token");
    authData.put("expires_in", 3123321378374l);
    authData.put("openid", "weixinopenid");
  }

  private static final String PLATFORM = "weixinapp";

  public AVObjectACLTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(AVObjectACLTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    AVUser currentUser = AVUser.currentUser();
    if (null != currentUser) {
      AVUser.logOut();
    }
  }

  public void testPrepareACL() throws Exception {
    AVQuery query = AVRole.getQuery();
    List<AVRole> admins = query.whereEqualTo("name", "Administrator").find();
    if (null != admins && admins.size() > 0) {
      System.out.println("User and Role already exist.");
      return;
    }

    AVUser avUser = AVUser.loginWithAuthData(authData, PLATFORM).blockingFirst();

    AVACL roleACL = new AVACL();
    roleACL.setPublicReadAccess(true);
    roleACL.setPublicWriteAccess(true);
    AVRole administrator= new AVRole("Administrator", roleACL);
    administrator.getUsers().add(avUser);
    administrator.saveInBackground().blockingFirst();
  }

  public void testPublicReadWriteWithUnauth() throws Exception {
    AVObject object = new AVObject("PublicReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            object1.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testOwnerReadWriteWithUnauth() throws Exception {
    AVObject object = new AVObject("OwnerReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        final AVObject tmp = avObject;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });

          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }


  public void testOwnerStrictReadWriteWithUnauth() throws Exception {
    AVObject object = new AVObject("OwnerStrictReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        final AVObject tmp = avObject;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });

          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testPublicReadWriteWithAuthUser() throws Exception {
    AVUser user = AVUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    AVObject object = new AVObject("PublicReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            object1.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testOwnerReadWriteWithAuthUser() throws Exception {
    AVUser user = AVUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    AVObject object = new AVObject("OwnerReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        final AVObject tmp = avObject;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });

          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }


  public void testOwnerStrictReadWriteWithAuthUser() throws Exception {
    AVUser user = AVUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    AVObject object = new AVObject("OwnerStrictReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        final AVObject tmp = avObject;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());

                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            latch.countDown();


          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testStrictAllReadWithUnAuth() throws Exception {
    final AVObject object = new AVObject("StrictAllReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        object.put("age", 18);
        object.setFetchWhenSave(true);
        final AVObject tmp = object;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {

                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });

          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
        latch.countDown();

      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(testSucceed);
  }

  public void testStrictAllReadWithAuthUser() throws Exception {
    AVUser user = AVUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    AVObject object = new AVObject("StrictAllReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(AVObject avObject) {
        System.out.println("first create: " + avObject.toJSONString());
        avObject.put("age", 18);
        avObject.setFetchWhenSave(true);
        final AVObject tmp = avObject;
        tmp.saveInBackground().subscribe(new Observer<AVObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(AVObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();

          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());

            tmp.deleteInBackground().subscribe(new Observer<AVNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(AVNull avNull) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable throwable) {
                System.out.println("error on delete. cause:" + throwable.getMessage());
                testSucceed = true;
                latch.countDown();
              }

              @Override
              public void onComplete() {

              }
            });

          }

          @Override
          public void onComplete() {

          }
        });
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("error on create. cause:" + throwable.getMessage());
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
