package cn.leancloud;

import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LCObjectACLTest extends TestCase {
  private CountDownLatch latch = null;
  private boolean testSucceed = false;

  private static Map<String, Object> authData = new HashMap<>();
  static {
    authData.put("access_token", "weixin access token");
    authData.put("expires_in", 3123321378374l);
    authData.put("openid", "weixinopenid");
  }

  private static final String PLATFORM = "weixinapp";

  public LCObjectACLTest(String testName) {
    super(testName);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(LCObjectACLTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    latch = new CountDownLatch(1);
    testSucceed = false;
    LCUser currentUser = LCUser.currentUser();
    if (null != currentUser) {
      LCUser.logOut();
    }
  }

  public void testPrepareACL() throws Exception {
    LCQuery query = LCRole.getQuery();
    List<LCRole> admins = query.whereEqualTo("name", "Administrator").find();
    if (null != admins && admins.size() > 0) {
      System.out.println("User and Role already exist.");
      return;
    }

    LCUser avUser = LCUser.loginWithAuthData(authData, PLATFORM).blockingFirst();

    LCACL roleACL = new LCACL();
    roleACL.setPublicReadAccess(true);
    roleACL.setPublicWriteAccess(true);
    LCRole administrator= new LCRole("Administrator", roleACL);
    administrator.getUsers().add(avUser);
    administrator.saveInBackground().blockingFirst();
  }

  public void testPublicReadWriteWithUnauth() throws Exception {
    LCObject object = new LCObject("PublicReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            object1.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    LCObject object = new LCObject("OwnerReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        final LCObject tmp = LCObject;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    LCObject object = new LCObject("OwnerStrictReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        final LCObject tmp = LCObject;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    LCUser user = LCUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    LCObject object = new LCObject("PublicReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        LCObject.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            object1.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    LCUser user = LCUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    LCObject object = new LCObject("OwnerReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        final LCObject tmp = LCObject;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    LCUser user = LCUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    LCObject object = new LCObject("OwnerStrictReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        final LCObject tmp = LCObject;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
    final LCObject object = new LCObject("StrictAllReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        object.put("age", 18);
        object.setFetchWhenSave(true);
        final LCObject tmp = object;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());
            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {

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
    LCUser user = LCUser.loginWithAuthData(authData, PLATFORM).blockingFirst();
    System.out.println(user.toJSONString());

    LCObject object = new LCObject("StrictAllReadWrite");
    object.put("age", 20);
    object.put("content", "Automatic Tester");
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<LCObject>() {
      @Override
      public void onSubscribe(Disposable disposable) {
      }

      @Override
      public void onNext(LCObject LCObject) {
        System.out.println("first create: " + LCObject.toJSONString());
        LCObject.put("age", 18);
        LCObject.setFetchWhenSave(true);
        final LCObject tmp = LCObject;
        tmp.saveInBackground().subscribe(new Observer<LCObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(LCObject object1) {
            System.out.println("second update: " + object1.toJSONString());
            latch.countDown();

          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("error on update. cause:" + throwable.getMessage());

            tmp.deleteInBackground().subscribe(new Observer<LCNull>() {
              @Override
              public void onSubscribe(Disposable disposable) {

              }

              @Override
              public void onNext(LCNull LCNull) {
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
