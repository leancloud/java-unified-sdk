package cn.leancloud;

import cn.leancloud.core.AVOSCloud;
import cn.leancloud.types.AVNull;
import com.alibaba.fastjson.JSONObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Arrays;

public class AVObjectTest extends TestCase {
  public AVObjectTest(String testName) {
    super(testName);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }
  public static Test suite() {
    return new TestSuite(AVObjectTest.class);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testDeleteField() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 18);
    object.put("grade", 9);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("try to remove grade field.");
        avObject.remove("grade");
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {
          }

          public void onNext(AVObject avObject) {
            System.out.println("remove field finished.");
            avObject.deleteInBackground().subscribe();
          }

          public void onError(Throwable throwable) {
            fail();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {

      }

      public void onComplete() {

      }
    });
  }

  public void testCreateObjectWithPublicACL() {
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.addUnique("course", Arrays.asList("Math", "Reading"));
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
            avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(AVNull aVoid) {
                System.out.println("delete object finished!");
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

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });

  }

  public void testCreateObjectWithReadOnlyACL() {
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(false);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("[Thread:" + Thread.currentThread().getId() +
                "]update object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
        avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVNull aVoid) {
            System.out.println("delete object finished, but ACL doesn't work!");
            fail();
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed as expected.");
            assertNotNull(throwable);
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

  public void testCreateObject() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 17);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {
      }

      public void onNext(AVObject avObject) {
        System.out.println("create object finished. objectId=" + avObject.getObjectId() + ", className=" + avObject.getClassName());
        avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVNull aVoid) {
            System.out.println("delete object finished.");
          }

          public void onError(Throwable throwable) {
            System.out.println("delete object failed.");
            fail();
          }

          public void onComplete() {
          }
        });

      }

      public void onError(Throwable throwable) {
        System.out.println("create object failed.");

      }

      public void onComplete() {
      }
    });
  }

  public void testObjectRefresh() {
    AVObject object = new AVObject("Student");
    object.setObjectId("5a7a4ac8128fe1003768d2b1");
    object.refreshInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("subscribe result: " + avObject.toString());
      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {
        System.out.println("subscribe completed.");
      }
    });
    System.out.println("test completed.");
  }

  public void testIncrementOperation() {
    AVObject object = new AVObject("Student");
    object.setObjectId("5ab5f7b89f545437fe95f860");
    object.increment("age", 5);
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        System.out.println("new value of age: " + avObject.getInt("age"));
        avObject.decrement("age");
        avObject.setFetchWhenSave(false);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {
            ;
          }

          public void onNext(AVObject avObject) {
            System.out.println("new value of age: " + avObject.getInt("age"));
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

  public void testCreateObjectThenBitOperation() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 39);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.bitAnd("age", 0x32);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            avObject.bitOr("age", 0x12);
            avObject.saveInBackground().subscribe(new Observer<AVObject>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(AVObject avObject) {
                avObject.bitXor("age", 0x3208);
                avObject.saveInBackground().subscribe(new Observer<AVObject>() {
                  public void onSubscribe(Disposable disposable) {

                  }

                  public void onNext(AVObject avObject) {
                    avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
                      public void onSubscribe(Disposable disposable) {

                      }

                      public void onNext(AVNull avNull) {
                        System.out.println("OK!");
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

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });

  }
  public void testCreateObjectThenRemoveOperation() {
    AVACL acl = new AVACL();
    acl.setPublicWriteAccess(true);
    acl.setPublicReadAccess(true);
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.setACL(acl);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.removeAll("course", Arrays.asList("Math", "Reading"));
        avObject.setFetchWhenSave(true);
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject2) {
            System.out.println("[Thread:" + Thread.currentThread().getId() +
                    "]update object finished. objectId=" + avObject2.getObjectId() + ", className=" + avObject2.getClassName());
            System.out.println(avObject2.get("course").toString());
            avObject2.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {
                ;
              }

              public void onNext(AVNull aVoid) {
                System.out.println("delete object finished!");
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

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }

  public void testCompoundOperation() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.put("course", Arrays.asList("Math", "Science"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {
        avObject.add("course", "Sports");
        avObject.addUnique("course", "Art");
        avObject.removeAll("course", Arrays.asList("Math"));
        avObject.removeAll("course", Arrays.asList("Sports"));
        avObject.saveInBackground().subscribe(new Observer<AVObject>() {
          public void onSubscribe(Disposable disposable) {

          }

          public void onNext(AVObject avObject) {
            avObject.deleteInBackground().subscribe(new Observer<AVNull>() {
              public void onSubscribe(Disposable disposable) {

              }

              public void onNext(AVNull avNull) {

              }

              public void onError(Throwable throwable) {
                fail();
              }

              public void onComplete() {

              }
            });
          }

          public void onError(Throwable throwable) {
            fail();;
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

  public void testBatchSaveOperation() {
    AVObject object = new AVObject("Student");
    object.put("name", "Automatic Tester");
    object.put("age", 19);
    object.add("course", "Art");
    object.removeAll("course", Arrays.asList("Math"));
    object.setFetchWhenSave(true);
    object.saveInBackground().subscribe(new Observer<AVObject>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVObject avObject) {

      }

      public void onError(Throwable throwable) {
        fail();
      }

      public void onComplete() {

      }
    });
  }
}
